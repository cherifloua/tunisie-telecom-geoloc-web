package com.telecomapp.web.model;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Persistence layer for Site objects.
 *
 * Deux modes, choisis automatiquement au demarrage :
 *
 *  - Variable d'environnement DATABASE_URL presente -> mode POSTGRES : la
 *    base tourne chez un hebergeur externe (Render/Supabase/Neon/Railway...,
 *    plan gratuit possible). C'est le mode a utiliser en production : les
 *    donnees survivent aux redeploiements et sont vues en temps reel par
 *    tous les appareils (PC, telephone...) qui parlent au meme serveur.
 *
 *  - DATABASE_URL absente -> mode SQLITE (fichier local data/sites.db).
 *    Pratique pour developper/tester en local sans rien configurer.
 *
 * Migration automatique et sans perte au demarrage :
 *    ancien CSV (sites.csv) -> SQLite (sites.db) -> Postgres (DATABASE_URL)
 * Chaque etape ne s'execute que si la cible est vide, donc rejouable sans
 * risque a chaque demarrage.
 */
@Component
public class SiteRepository {

    private enum Dialect { SQLITE, POSTGRES }

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path SQLITE_FILE = DATA_DIR.resolve("sites.db");
    private static final Path LEGACY_CSV_FILE = DATA_DIR.resolve("sites.csv");

    private final Dialect dialect;
    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPassword;

    public SiteRepository() {
        String databaseUrl = System.getenv("DATABASE_URL");
        try {
            if (databaseUrl != null && !databaseUrl.isBlank()) {
                this.dialect = Dialect.POSTGRES;
                PgConn pg = parsePostgresUrl(databaseUrl);
                this.jdbcUrl = pg.jdbcUrl;
                this.jdbcUser = pg.user;
                this.jdbcPassword = pg.password;
                Class.forName("org.postgresql.Driver");
                System.out.println("SiteRepository : base PostgreSQL externe utilisee (DATABASE_URL detectee).");
            } else {
                this.dialect = Dialect.SQLITE;
                Files.createDirectories(DATA_DIR);
                this.jdbcUrl = "jdbc:sqlite:" + SQLITE_FILE;
                this.jdbcUser = null;
                this.jdbcPassword = null;
                Class.forName("org.sqlite.JDBC");
                System.out.println("SiteRepository : base SQLite locale utilisee (" + SQLITE_FILE + "). "
                        + "Definis la variable d'environnement DATABASE_URL pour passer sur une base en ligne persistante.");
            }
            initSchema();
            migrateLegacyCsvIntoSqliteIfNeeded();
            migrateSqliteIntoPostgresIfNeeded();
            if (findAll().isEmpty()) {
                seedSampleData();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Impossible d'initialiser la base de donnees", e);
        }
    }

    // ---------- Connexion ----------

    private Connection connect() throws SQLException {
        Connection c = (jdbcUser != null)
                ? DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)
                : DriverManager.getConnection(jdbcUrl);
        if (dialect == Dialect.SQLITE) {
            try (Statement s = c.createStatement()) {
                // Meilleure tolerance aux acces concurrents PC + telephone sur le meme fichier.
                s.execute("PRAGMA journal_mode=WAL");
                s.execute("PRAGMA foreign_keys=ON");
            }
        }
        return c;
    }

    /**
     * Convertit une URL "postgres://user:pass@host:port/db?..." (format
     * standard fourni par Render/Railway/Supabase/Neon/Heroku...) en URL
     * JDBC + identifiants separes. Ajoute sslmode=require par defaut, car
     * la quasi-totalite des offres gratuites exigent une connexion chiffree.
     */
    private static PgConn parsePostgresUrl(String databaseUrl) {
        try {
            String normalized = databaseUrl.startsWith("jdbc:") ? databaseUrl.substring(5) : databaseUrl;
            URI uri = new URI(normalized);
            String userInfo = uri.getUserInfo();
            String user = null, password = null;
            if (userInfo != null && userInfo.contains(":")) {
                String[] parts = userInfo.split(":", 2);
                user = parts[0];
                password = parts[1];
            }
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String query = uri.getQuery();
            boolean hasSslMode = query != null && query.toLowerCase().contains("sslmode");
            StringBuilder sb = new StringBuilder("jdbc:postgresql://")
                    .append(uri.getHost()).append(":").append(port).append(uri.getPath());
            List<String> params = new ArrayList<>();
            if (query != null && !query.isBlank()) params.add(query);
            if (!hasSslMode) params.add("sslmode=require");
            if (!params.isEmpty()) sb.append("?").append(String.join("&", params));
            PgConn pg = new PgConn();
            pg.jdbcUrl = sb.toString();
            pg.user = user;
            pg.password = password;
            return pg;
        } catch (Exception e) {
            throw new IllegalStateException("DATABASE_URL invalide : " + databaseUrl, e);
        }
    }

    private static class PgConn {
        String jdbcUrl;
        String user;
        String password;
    }

    // ---------- Schema ----------

    private void initSchema() throws SQLException {
        String idColumn = (dialect == Dialect.POSTGRES)
                ? "id SERIAL PRIMARY KEY"
                : "id INTEGER PRIMARY KEY AUTOINCREMENT";
        String numType = (dialect == Dialect.POSTGRES) ? "DOUBLE PRECISION" : "REAL";
        String ddl = "CREATE TABLE IF NOT EXISTS sites (" +
                idColumn + "," +
                "nom TEXT," +
                "type TEXT," +
                "latitude " + numType + "," +
                "longitude " + numType + "," +
                "adresse TEXT," +
                "etat TEXT," +
                "region TEXT," +
                "altitude " + numType + "," +
                "type_support TEXT," +
                "hauteur_pylone " + numType + "," +
                "nombre_antennes INTEGER," +
                "hba " + numType + "," +
                "hauteur_antenne_sur_pylone " + numType + "," +
                "azimut " + numType + "," +
                "tilt_mecanique " + numType + "," +
                "tilt_electrique " + numType + "," +
                "gain_antenne " + numType + "," +
                "polarisation TEXT," +
                "bande_frequence TEXT," +
                "largeur_bande " + numType + "," +
                "puissance_emission " + numType + "," +
                "type_equipement_radio TEXT," +
                "rayon_couverture " + numType + "," +
                "type_alimentation TEXT," +
                "presence_onduleur INTEGER," +
                "presence_panneaux_solaires INTEGER," +
                "date_mise_en_service TEXT," +
                "date_derniere_maintenance TEXT," +
                "date_derniere_maj TEXT," +
                "observations TEXT," +
                "historique TEXT," +
                "cellule TEXT," +
                "canal TEXT," +
                "cid TEXT," +
                "pci TEXT" +
                ")";
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.execute(ddl);
        }
    }

    /**
     * Anciennes installations : si data/sites.csv existe encore et que la
     * base cible (SQLite ou Postgres) est vide, on l'importe puis on
     * renomme le fichier en .bak pour ne plus le relire au demarrage
     * suivant. Ne s'applique qu'au premier niveau de la chaine de migration.
     */
    private void migrateLegacyCsvIntoSqliteIfNeeded() throws IOException, SQLException {
        if (dialect != Dialect.SQLITE) return; // la migration CSV->Postgres direct n'est pas geree, on passe par SQLite
        if (!Files.exists(LEGACY_CSV_FILE)) return;
        if (!findAll().isEmpty()) return;

        List<String> lines = Files.readAllLines(LEGACY_CSV_FILE);
        int migrated = 0;
        for (String line : lines) {
            if (line.isBlank()) continue;
            insert(Site.fromCsvLine(line));
            migrated++;
        }
        Path backup = DATA_DIR.resolve("sites.csv.bak");
        Files.move(LEGACY_CSV_FILE, backup, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Migration CSV -> SQLite terminee (" + migrated
                + " site(s) importe(s)). Ancien fichier conserve sous " + backup);
    }

    /**
     * Passage a une base Postgres en ligne : si un fichier SQLite local
     * (data/sites.db, cree par un lancement precedent en local) existe et
     * que la base Postgres est vide, on y recopie tous les sites, pour ne
     * pas perdre le travail deja saisi en local au moment de basculer en
     * ligne.
     */
    private void migrateSqliteIntoPostgresIfNeeded() throws SQLException {
        if (dialect != Dialect.POSTGRES) return;
        if (!Files.exists(SQLITE_FILE)) return;
        if (!findAll().isEmpty()) return;

        List<Site> localSites = new ArrayList<>();
        String sqliteUrl = "jdbc:sqlite:" + SQLITE_FILE;
        try (Connection sc = DriverManager.getConnection(sqliteUrl);
             Statement st = sc.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM sites ORDER BY id")) {
            while (rs.next()) localSites.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Migration SQLite -> Postgres ignoree (base locale illisible) : " + e.getMessage());
            return;
        }
        for (Site s : localSites) insert(s);

        Path backup = DATA_DIR.resolve("sites.db.bak");
        try {
            Files.move(SQLITE_FILE, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // pas bloquant si le renommage echoue (ex: droits d'ecriture)
        }
        System.out.println("Migration SQLite -> Postgres terminee (" + localSites.size() + " site(s) importe(s)).");
    }

    // ---------- API publique (inchangee) ----------

    public synchronized List<Site> findAll() {
        String sql = "SELECT * FROM sites ORDER BY id";
        List<Site> result = new ArrayList<>();
        try (Connection c = connect(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lecture base de donnees", e);
        }
        return result;
    }

    public synchronized List<Site> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim();
        if (k.isEmpty()) return findAll();
        String sql = "SELECT * FROM sites WHERE " +
                "lower(nom) LIKE ? OR " +
                "lower(adresse) LIKE ? OR " +
                "lower(type) LIKE ? OR " +
                "lower(coalesce(region,'')) LIKE ? OR " +
                "lower(cellule) LIKE ? OR " +
                "lower(canal) LIKE ? OR " +
                "lower(cid) LIKE ? OR " +
                "lower(pci) LIKE ? " +
                "ORDER BY id";
        String like = "%" + k.toLowerCase() + "%";
        List<Site> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 1; i <= 8; i++) ps.setString(i, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur recherche base de donnees", e);
        }
        return result;
    }

    /** Recherche dediee au canal/BCCH (colonne "canal") : correspondance exacte OU partielle, utilisee par la carte GPS. */
    public synchronized List<Site> searchByCanal(String canal) {
        String k = canal == null ? "" : canal.trim();
        if (k.isEmpty()) return findAll();
        String sql = "SELECT * FROM sites WHERE lower(canal) LIKE ? ORDER BY id";
        List<Site> result = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + k.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur recherche BCCH", e);
        }
        return result;
    }

    public synchronized void add(Site site) {
        try {
            site.setId(insert(site));
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur ajout base de donnees", e);
        }
    }

    public synchronized void update(Site updated) {
        String sql = "UPDATE sites SET " +
                "nom=?, type=?, latitude=?, longitude=?, adresse=?, etat=?, region=?, altitude=?, " +
                "type_support=?, hauteur_pylone=?, nombre_antennes=?, hba=?, hauteur_antenne_sur_pylone=?, " +
                "azimut=?, tilt_mecanique=?, tilt_electrique=?, gain_antenne=?, polarisation=?, " +
                "bande_frequence=?, largeur_bande=?, puissance_emission=?, type_equipement_radio=?, " +
                "rayon_couverture=?, type_alimentation=?, presence_onduleur=?, presence_panneaux_solaires=?, " +
                "date_mise_en_service=?, date_derniere_maintenance=?, date_derniere_maj=?, observations=?, " +
                "historique=?, cellule=?, canal=?, cid=?, pci=? " +
                "WHERE id=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            bindSiteFields(ps, updated);
            ps.setInt(36, updated.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur mise a jour base de donnees", e);
        }
    }

    public synchronized void delete(int id) {
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement("DELETE FROM sites WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur suppression base de donnees", e);
        }
    }

    public synchronized Optional<Site> findById(int id) {
        String sql = "SELECT * FROM sites WHERE id=?";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur lecture base de donnees", e);
        }
        return Optional.empty();
    }

    // ---------- Helpers ----------

    private int insert(Site site) throws SQLException {
        String sql = "INSERT INTO sites (" +
                "nom, type, latitude, longitude, adresse, etat, region, altitude, " +
                "type_support, hauteur_pylone, nombre_antennes, hba, hauteur_antenne_sur_pylone, " +
                "azimut, tilt_mecanique, tilt_electrique, gain_antenne, polarisation, " +
                "bande_frequence, largeur_bande, puissance_emission, type_equipement_radio, " +
                "rayon_couverture, type_alimentation, presence_onduleur, presence_panneaux_solaires, " +
                "date_mise_en_service, date_derniere_maintenance, date_derniere_maj, observations, " +
                "historique, cellule, canal, cid, pci" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = connect(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindSiteFields(ps, site);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Aucun ID genere lors de l'insertion");
    }

    /** Bind les 35 colonnes (hors id) dans l'ordre utilise par insert()/update(). */
    private void bindSiteFields(PreparedStatement ps, Site s) throws SQLException {
        int i = 1;
        ps.setString(i++, s.getNom());
        ps.setString(i++, s.getType());
        ps.setDouble(i++, s.getLatitude());
        ps.setDouble(i++, s.getLongitude());
        ps.setString(i++, s.getAdresse());
        ps.setString(i++, s.getEtat());
        ps.setString(i++, s.getRegion());
        ps.setDouble(i++, s.getAltitude());
        ps.setString(i++, s.getTypeSupport());
        ps.setDouble(i++, s.getHauteurPylone());
        ps.setInt(i++, s.getNombreAntennes());
        ps.setDouble(i++, s.getHba());
        ps.setDouble(i++, s.getHauteurAntenneSurPylone());
        ps.setDouble(i++, s.getAzimut());
        ps.setDouble(i++, s.getTiltMecanique());
        ps.setDouble(i++, s.getTiltElectrique());
        ps.setDouble(i++, s.getGainAntenne());
        ps.setString(i++, s.getPolarisation());
        ps.setString(i++, s.getBandeFrequence());
        ps.setDouble(i++, s.getLargeurBande());
        ps.setDouble(i++, s.getPuissanceEmission());
        ps.setString(i++, s.getTypeEquipementRadio());
        ps.setDouble(i++, s.getRayonCouverture());
        ps.setString(i++, s.getTypeAlimentation());
        ps.setInt(i++, s.isPresenceOnduleur() ? 1 : 0);
        ps.setInt(i++, s.isPresencePanneauxSolaires() ? 1 : 0);
        ps.setString(i++, s.getDateMiseEnService() == null ? null : s.getDateMiseEnService().toString());
        ps.setString(i++, s.getDateDerniereMaintenance() == null ? null : s.getDateDerniereMaintenance().toString());
        ps.setString(i++, s.getDateDerniereMiseAJour() == null ? null : s.getDateDerniereMiseAJour().toString());
        ps.setString(i++, s.getObservations());
        ps.setString(i++, s.getHistorique());
        ps.setString(i++, s.getCellule());
        ps.setString(i++, s.getCanal());
        ps.setString(i++, s.getCid());
        ps.setString(i, s.getPci());
    }

    private Site mapRow(ResultSet rs) throws SQLException {
        Site s = new Site(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("type"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getString("adresse"),
                rs.getString("etat"),
                rs.getString("region"),
                rs.getDouble("altitude"),
                rs.getString("type_support"),
                rs.getDouble("hauteur_pylone"),
                rs.getInt("nombre_antennes"),
                rs.getDouble("hba"),
                rs.getDouble("hauteur_antenne_sur_pylone"),
                rs.getDouble("azimut"),
                rs.getDouble("tilt_mecanique"),
                rs.getDouble("tilt_electrique"),
                rs.getDouble("gain_antenne"),
                rs.getString("polarisation"),
                rs.getString("bande_frequence"),
                rs.getDouble("largeur_bande"),
                rs.getDouble("puissance_emission"),
                rs.getString("type_equipement_radio"),
                rs.getDouble("rayon_couverture"),
                rs.getString("type_alimentation"),
                rs.getInt("presence_onduleur") != 0,
                rs.getInt("presence_panneaux_solaires") != 0,
                parseDate(rs.getString("date_mise_en_service")),
                parseDate(rs.getString("date_derniere_maintenance")),
                parseDate(rs.getString("date_derniere_maj")),
                rs.getString("observations")
        );
        s.setHistorique(rs.getString("historique"));
        s.setCellule(rs.getString("cellule"));
        s.setCanal(rs.getString("canal"));
        s.setCid(rs.getString("cid"));
        s.setPci(rs.getString("pci"));
        return s;
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private void seedSampleData() {
        add(new Site(0, "Antenne Kairouan Nord", "4G", 35.6789, 10.0965, "Kairouan", "Actif",
                "Kairouan", 65.0,
                "Pylone autoportant", 30.0, 3,
                95.0, 28.0, 120.0, 2.0, 4.0, 17.5, "+/-45",
                "1800", 20.0, 43.0, "eNodeB", 5.0,
                "Reseau electrique", true, false,
                java.time.LocalDate.of(2020, 3, 15), java.time.LocalDate.of(2025, 11, 2),
                java.time.LocalDate.of(2025, 11, 2),
                "Site prioritaire, forte affluence en centre-ville"));
        add(new Site(0, "Centrale Telecom Sousse", "5G", 35.8256, 10.6084, "Sousse", "Actif",
                "Sousse", 12.0,
                "Rooftop", 18.0, 4,
                40.0, 20.0, 200.0, 0.0, 6.0, 20.0, "+/-45",
                "3500", 100.0, 40.0, "gNodeB", 2.5,
                "Reseau electrique", true, true,
                java.time.LocalDate.of(2022, 6, 1), java.time.LocalDate.of(2026, 1, 10),
                java.time.LocalDate.of(2026, 1, 10),
                "Zone touristique, deploiement 5G recent"));
        add(new Site(0, "Antenne Tunis Centre", "3G", 36.8065, 10.1815, "Tunis", "En panne",
                "Tunis", 25.0,
                "Pylone haubane", 40.0, 2,
                70.0, 32.0, 45.0, 3.0, 2.0, 16.0, "Verticale",
                "2100", 15.0, 41.0, "Node B", 3.0,
                "Groupe electrogene", false, false,
                java.time.LocalDate.of(2015, 9, 20), java.time.LocalDate.of(2026, 5, 18),
                java.time.LocalDate.of(2026, 5, 18),
                "Panne signalee : coupure d'alimentation, intervention prevue"));
        add(new Site(0, "Antenne Sfax Sud", "2G", 34.7406, 10.7603, "Sfax", "Actif",
                "Sfax", 8.0,
                "Monopole", 25.0, 1,
                50.0, 22.0, 300.0, 1.0, 0.0, 14.0, "Horizontale",
                "900", 10.0, 39.0, "BTS", 6.0,
                "Batterie", false, true,
                java.time.LocalDate.of(2010, 1, 5), java.time.LocalDate.of(2025, 8, 30),
                java.time.LocalDate.of(2025, 8, 30),
                "Site rural, couverture etendue"));
    }
}
