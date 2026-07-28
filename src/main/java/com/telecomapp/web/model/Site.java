package com.telecomapp.web.model;

import java.time.LocalDate;

/**
 * Represents a telecom site (antenna / equipment) with its GPS position and
 * its complete technical sheet (infrastructure, antenna, radio, power and
 * operational parameters).
 *
 * The original fields (id, nom, type, latitude, longitude, adresse, etat) are
 * kept exactly as before so that every existing feature (CRUD, map, search,
 * KML export, technology colors/status) keeps working unchanged. All new
 * fields below are additions only.
 */
public class Site {

    // ---------- Informations generales (existant) ----------
    private int id;
    private String nom;
    private String type;      // 2G, 3G, 4G, 5G
    private double latitude;
    private double longitude;
    private String adresse;
    private String etat;      // Actif, Maintenance, En panne

    // ---------- Informations generales (ajout) ----------
    private String region;    // Region / Gouvernorat

    // ---------- Coordonnees geographiques (ajout) ----------
    private double altitude;  // metres

    // ---------- Infrastructure (ajout) ----------
    private String typeSupport;      // Pylone autoportant, Pylone haubane, Rooftop, Monopole
    private double hauteurPylone;     // metres
    private int nombreAntennes;

    // ---------- Parametres antennaires (ajout) ----------
    private double hba;                       // Height Above Average Terrain
    private double hauteurAntenneSurPylone;   // metres
    private double azimut;                    // degres (0-360)
    private double tiltMecanique;              // degres
    private double tiltElectrique;             // degres
    private double gainAntenne;                // dBi
    private String polarisation;               // ex: +/-45, Verticale, Horizontale

    // ---------- Parametres radio (ajout, "type" ci-dessus reste la technologie) ----------
    private String bandeFrequence;      // ex: 900 MHz, 1800 MHz, 2100 MHz...
    private double largeurBande;        // MHz
    private double puissanceEmission;   // dBm
    private String typeEquipementRadio; // ex: RRU, BBU, Node B, eNodeB, gNodeB
    private double rayonCouverture;     // km (estimation)

    // ---------- Parametres energetiques (ajout) ----------
    private String typeAlimentation;    // Reseau electrique, Batterie, Groupe electrogene
    private boolean presenceOnduleur;
    private boolean presencePanneauxSolaires;

    // ---------- Parametres operationnels (ajout, "etat" ci-dessus reste le statut) ----------
    private LocalDate dateMiseEnService;
    private LocalDate dateDerniereMaintenance;
    private LocalDate dateDerniereMiseAJour;
    private String observations;

    // Historique des mises a jour : journal en texte libre, entrees separees par
    // " | ", la plus recente en premier. Alimente automatiquement a chaque
    // creation/modification (voir SiteController), jamais saisi manuellement.
    private String historique = "";

    /**
     * Constructeur complet (toutes les informations techniques).
     */
    public Site(int id, String nom, String type, double latitude, double longitude, String adresse, String etat,
                String region, double altitude,
                String typeSupport, double hauteurPylone, int nombreAntennes,
                double hba, double hauteurAntenneSurPylone, double azimut, double tiltMecanique,
                double tiltElectrique, double gainAntenne, String polarisation,
                String bandeFrequence, double largeurBande, double puissanceEmission,
                String typeEquipementRadio, double rayonCouverture,
                String typeAlimentation, boolean presenceOnduleur, boolean presencePanneauxSolaires,
                LocalDate dateMiseEnService, LocalDate dateDerniereMaintenance,
                LocalDate dateDerniereMiseAJour, String observations) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adresse = adresse;
        this.etat = etat;
        this.region = region;
        this.altitude = altitude;
        this.typeSupport = typeSupport;
        this.hauteurPylone = hauteurPylone;
        this.nombreAntennes = nombreAntennes;
        this.hba = hba;
        this.hauteurAntenneSurPylone = hauteurAntenneSurPylone;
        this.azimut = azimut;
        this.tiltMecanique = tiltMecanique;
        this.tiltElectrique = tiltElectrique;
        this.gainAntenne = gainAntenne;
        this.polarisation = polarisation;
        this.bandeFrequence = bandeFrequence;
        this.largeurBande = largeurBande;
        this.puissanceEmission = puissanceEmission;
        this.typeEquipementRadio = typeEquipementRadio;
        this.rayonCouverture = rayonCouverture;
        this.typeAlimentation = typeAlimentation;
        this.presenceOnduleur = presenceOnduleur;
        this.presencePanneauxSolaires = presencePanneauxSolaires;
        this.dateMiseEnService = dateMiseEnService;
        this.dateDerniereMaintenance = dateDerniereMaintenance;
        this.dateDerniereMiseAJour = dateDerniereMiseAJour;
        this.observations = observations;
    }

    /**
     * Constructeur simplifie (identique a la version originale de l'application),
     * conserve pour compatibilite : remplit les nouveaux champs avec des valeurs
     * par defaut neutres.
     */
    public Site(int id, String nom, String type, double latitude, double longitude,
                String adresse, String etat) {
        this(id, nom, type, latitude, longitude, adresse, etat,
                "", 0.0,
                "Pylone autoportant", 0.0, 1,
                0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, "Verticale",
                "", 0.0, 0.0,
                "", 0.0,
                "Reseau electrique", false, false,
                null, null, null, "");
    }

    // ---------- Getters / setters : champs existants ----------
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    // ---------- Getters / setters : nouveaux champs ----------
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public double getAltitude() { return altitude; }
    public void setAltitude(double altitude) { this.altitude = altitude; }

    public String getTypeSupport() { return typeSupport; }
    public void setTypeSupport(String typeSupport) { this.typeSupport = typeSupport; }

    public double getHauteurPylone() { return hauteurPylone; }
    public void setHauteurPylone(double hauteurPylone) { this.hauteurPylone = hauteurPylone; }

    public int getNombreAntennes() { return nombreAntennes; }
    public void setNombreAntennes(int nombreAntennes) { this.nombreAntennes = nombreAntennes; }

    public double getHba() { return hba; }
    public void setHba(double hba) { this.hba = hba; }

    public double getHauteurAntenneSurPylone() { return hauteurAntenneSurPylone; }
    public void setHauteurAntenneSurPylone(double v) { this.hauteurAntenneSurPylone = v; }

    public double getAzimut() { return azimut; }
    public void setAzimut(double azimut) { this.azimut = azimut; }

    public double getTiltMecanique() { return tiltMecanique; }
    public void setTiltMecanique(double tiltMecanique) { this.tiltMecanique = tiltMecanique; }

    public double getTiltElectrique() { return tiltElectrique; }
    public void setTiltElectrique(double tiltElectrique) { this.tiltElectrique = tiltElectrique; }

    public double getGainAntenne() { return gainAntenne; }
    public void setGainAntenne(double gainAntenne) { this.gainAntenne = gainAntenne; }

    public String getPolarisation() { return polarisation; }
    public void setPolarisation(String polarisation) { this.polarisation = polarisation; }

    public String getBandeFrequence() { return bandeFrequence; }
    public void setBandeFrequence(String bandeFrequence) { this.bandeFrequence = bandeFrequence; }

    public double getLargeurBande() { return largeurBande; }
    public void setLargeurBande(double largeurBande) { this.largeurBande = largeurBande; }

    public double getPuissanceEmission() { return puissanceEmission; }
    public void setPuissanceEmission(double puissanceEmission) { this.puissanceEmission = puissanceEmission; }

    public String getTypeEquipementRadio() { return typeEquipementRadio; }
    public void setTypeEquipementRadio(String v) { this.typeEquipementRadio = v; }

    public double getRayonCouverture() { return rayonCouverture; }
    public void setRayonCouverture(double rayonCouverture) { this.rayonCouverture = rayonCouverture; }

    public String getTypeAlimentation() { return typeAlimentation; }
    public void setTypeAlimentation(String typeAlimentation) { this.typeAlimentation = typeAlimentation; }

    public boolean isPresenceOnduleur() { return presenceOnduleur; }
    public void setPresenceOnduleur(boolean presenceOnduleur) { this.presenceOnduleur = presenceOnduleur; }

    public boolean isPresencePanneauxSolaires() { return presencePanneauxSolaires; }
    public void setPresencePanneauxSolaires(boolean v) { this.presencePanneauxSolaires = v; }

    public LocalDate getDateMiseEnService() { return dateMiseEnService; }
    public void setDateMiseEnService(LocalDate dateMiseEnService) { this.dateMiseEnService = dateMiseEnService; }

    public LocalDate getDateDerniereMaintenance() { return dateDerniereMaintenance; }
    public void setDateDerniereMaintenance(LocalDate d) { this.dateDerniereMaintenance = d; }

    public LocalDate getDateDerniereMiseAJour() { return dateDerniereMiseAJour; }
    public void setDateDerniereMiseAJour(LocalDate d) { this.dateDerniereMiseAJour = d; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getHistorique() { return historique == null ? "" : historique; }
    public void setHistorique(String historique) { this.historique = historique == null ? "" : historique; }

    /** Les entrees de l'historique, de la plus recente a la plus ancienne. */
    public java.util.List<String> historiqueEntries() {
        String h = getHistorique();
        if (h.isBlank()) return java.util.List.of();
        return java.util.Arrays.stream(h.split("\\|"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /** La derniere entree de l'historique (ex: "24/07/2026 : site modifie"), ou "-" si aucune. */
    public String latestHistoriqueEntry() {
        java.util.List<String> entries = historiqueEntries();
        return entries.isEmpty() ? "-" : entries.get(0);
    }

    // ---------- Methodes existantes (inchangees) ----------

    /** Returns a hex color code depending on the technology, used across the UI. */
    public String colorForType() {
        return switch (type) {
            case "2G" -> "#8e8e8e";
            case "3G" -> "#3498db";
            case "4G" -> "#27ae60";
            case "5G" -> "#e67e22";
            default -> "#000000";
        };
    }

    public boolean isActif() {
        return "Actif".equalsIgnoreCase(etat);
    }

    /** Couleur d'etat pour la carte GPS : vert=Actif, orange=Maintenance, rouge=En panne (ou autre). */
    public String colorForEtat() {
        if ("Actif".equalsIgnoreCase(etat)) return "#27ae60";
        if ("Maintenance".equalsIgnoreCase(etat)) return "#e67e22";
        return "#c0392b";
    }

    // ---------- Persistance CSV ----------
    // Le nombre de colonnes a augmente pour stocker les nouveaux parametres,
    // mais l'ordre des 7 premieres colonnes est conserve pour rester lisible.
    // Les valeurs texte pouvant contenir un ';' (observations) sont nettoyees.

    public String toCsvLine() {
        return String.join(";",
                String.valueOf(id),
                safe(nom),
                safe(type),
                String.valueOf(latitude),
                String.valueOf(longitude),
                safe(adresse),
                safe(etat),
                safe(region),
                String.valueOf(altitude),
                safe(typeSupport),
                String.valueOf(hauteurPylone),
                String.valueOf(nombreAntennes),
                String.valueOf(hba),
                String.valueOf(hauteurAntenneSurPylone),
                String.valueOf(azimut),
                String.valueOf(tiltMecanique),
                String.valueOf(tiltElectrique),
                String.valueOf(gainAntenne),
                safe(polarisation),
                safe(bandeFrequence),
                String.valueOf(largeurBande),
                String.valueOf(puissanceEmission),
                safe(typeEquipementRadio),
                String.valueOf(rayonCouverture),
                safe(typeAlimentation),
                String.valueOf(presenceOnduleur),
                String.valueOf(presencePanneauxSolaires),
                dateMiseEnService == null ? "" : dateMiseEnService.toString(),
                dateDerniereMaintenance == null ? "" : dateDerniereMaintenance.toString(),
                safe(observations),
                dateDerniereMiseAJour == null ? "" : dateDerniereMiseAJour.toString(),
                safe(historique)
        );
    }

    public static Site fromCsvLine(String line) {
        String[] p = line.split(";", -1);
        Site site = new Site(
                Integer.parseInt(get(p, 0, "0")),
                get(p, 1, ""),
                get(p, 2, ""),
                Double.parseDouble(get(p, 3, "0")),
                Double.parseDouble(get(p, 4, "0")),
                get(p, 5, ""),
                get(p, 6, "Actif"),
                get(p, 7, ""),
                parseDouble(get(p, 8, "0")),
                get(p, 9, "Pylone autoportant"),
                parseDouble(get(p, 10, "0")),
                (int) parseDouble(get(p, 11, "1")),
                parseDouble(get(p, 12, "0")),
                parseDouble(get(p, 13, "0")),
                parseDouble(get(p, 14, "0")),
                parseDouble(get(p, 15, "0")),
                parseDouble(get(p, 16, "0")),
                parseDouble(get(p, 17, "0")),
                get(p, 18, "Verticale"),
                get(p, 19, ""),
                parseDouble(get(p, 20, "0")),
                parseDouble(get(p, 21, "0")),
                get(p, 22, ""),
                parseDouble(get(p, 23, "0")),
                get(p, 24, "Reseau electrique"),
                Boolean.parseBoolean(get(p, 25, "false")),
                Boolean.parseBoolean(get(p, 26, "false")),
                parseDate(get(p, 27, "")),
                parseDate(get(p, 28, "")),
                parseDate(get(p, 30, "")),
                get(p, 29, "")
        );
        // Colonne ajoutee apres coup : absente sur les anciens fichiers CSV,
        // get() retombe alors simplement sur "" (aucune migration necessaire).
        site.setHistorique(get(p, 31, ""));
        return site;
    }

    private static String get(String[] p, int idx, String def) {
        return (idx < p.length && !p[idx].isBlank()) ? p[idx] : def;
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0.0; }
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private static String safe(String s) {
        // Le point-virgule est le separateur CSV : on le remplace pour ne pas casser le format.
        return s == null ? "" : s.replace(";", ",").replace("\n", " ").replace("\r", " ");
    }
}
