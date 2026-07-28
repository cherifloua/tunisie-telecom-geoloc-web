package com.telecomapp.web.web;

import com.telecomapp.web.model.Site;

import java.time.LocalDate;

/**
 * Objet simple utilise uniquement pour lier le formulaire HTML (Thymeleaf) aux
 * donnees. Un DTO dedie (plutot que Site directement) evite d'exiger un
 * constructeur vide sur le modele metier, et garde toute la logique de
 * validation/parsing au meme endroit (comme le faisait SiteFormPanel cote
 * desktop).
 */
public class SiteFormData {

    private int id;
    private String nom = "";
    private String type = "4G";
    private String etat = "Actif";
    private String region = "";
    private String adresse = "";
    private String latitude = "";
    private String longitude = "";
    private String altitude = "";

    private String typeSupport = "Pylone autoportant";
    private String hauteurPylone = "";
    private String nombreAntennes = "";

    private String hba = "";
    private String hauteurAntenneSurPylone = "";
    private String azimut = "";
    private String tiltMecanique = "";
    private String tiltElectrique = "";
    private String gainAntenne = "";
    private String polarisation = "+/-45";

    private String bandeFrequence = "";
    private String largeurBande = "";
    private String puissanceEmission = "";
    private String typeEquipementRadio = "";
    private String rayonCouverture = "";

    private String typeAlimentation = "Reseau electrique";
    private boolean presenceOnduleur;
    private boolean presencePanneauxSolaires;

    private String dateMiseEnService = "";
    private String dateDerniereMaintenance = "";
    private String dateDerniereMiseAJour = "";
    private String observations = "";
    private String nouvelleEntreeHistorique = "";

    public static SiteFormData fromSite(Site s) {
        SiteFormData f = new SiteFormData();
        f.id = s.getId();
        f.nom = s.getNom();
        f.type = s.getType();
        f.etat = s.getEtat();
        f.region = s.getRegion();
        f.adresse = s.getAdresse();
        f.latitude = String.valueOf(s.getLatitude());
        f.longitude = String.valueOf(s.getLongitude());
        f.altitude = String.valueOf(s.getAltitude());

        f.typeSupport = s.getTypeSupport();
        f.hauteurPylone = String.valueOf(s.getHauteurPylone());
        f.nombreAntennes = String.valueOf(s.getNombreAntennes());

        f.hba = String.valueOf(s.getHba());
        f.hauteurAntenneSurPylone = String.valueOf(s.getHauteurAntenneSurPylone());
        f.azimut = String.valueOf(s.getAzimut());
        f.tiltMecanique = String.valueOf(s.getTiltMecanique());
        f.tiltElectrique = String.valueOf(s.getTiltElectrique());
        f.gainAntenne = String.valueOf(s.getGainAntenne());
        f.polarisation = s.getPolarisation();

        f.bandeFrequence = s.getBandeFrequence();
        f.largeurBande = String.valueOf(s.getLargeurBande());
        f.puissanceEmission = String.valueOf(s.getPuissanceEmission());
        f.typeEquipementRadio = s.getTypeEquipementRadio();
        f.rayonCouverture = String.valueOf(s.getRayonCouverture());

        f.typeAlimentation = s.getTypeAlimentation();
        f.presenceOnduleur = s.isPresenceOnduleur();
        f.presencePanneauxSolaires = s.isPresencePanneauxSolaires();

        f.dateMiseEnService = s.getDateMiseEnService() == null ? "" : s.getDateMiseEnService().toString();
        f.dateDerniereMaintenance = s.getDateDerniereMaintenance() == null ? "" : s.getDateDerniereMaintenance().toString();
        f.dateDerniereMiseAJour = s.getDateDerniereMiseAJour() == null ? "" : s.getDateDerniereMiseAJour().toString();
        f.observations = s.getObservations();
        return f;
    }

    /**
     * Convertit le formulaire en Site.
     * Lance IllegalArgumentException si un champ obligatoire (nom, latitude,
     * longitude) est vide, ou NumberFormatException si un champ numerique est
     * invalide.
     */
    public Site toSite() {
        String cleanNom = nom == null ? "" : nom.trim();
        if (cleanNom.isEmpty()) {
            throw new IllegalArgumentException("le nom du site est obligatoire");
        }
        return new Site(id, cleanNom, type, parseRequired(latitude, "Latitude"),
                parseRequired(longitude, "Longitude"), adresse.trim(), etat,
                region.trim(), parseD(altitude),
                typeSupport, parseD(hauteurPylone), (int) parseD(nombreAntennes),
                parseD(hba), parseD(hauteurAntenneSurPylone), parseD(azimut), parseD(tiltMecanique),
                parseD(tiltElectrique), parseD(gainAntenne), polarisation,
                bandeFrequence.trim(), parseD(largeurBande), parseD(puissanceEmission),
                typeEquipementRadio.trim(), parseD(rayonCouverture),
                typeAlimentation, presenceOnduleur, presencePanneauxSolaires,
                parseDate(dateMiseEnService), parseDate(dateDerniereMaintenance),
                parseDate(dateDerniereMiseAJour), observations.trim());
    }

    private static double parseRequired(String s, String fieldName) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(fieldName + " est obligatoire");
        }
        return Double.parseDouble(s.trim().replace(",", "."));
    }

    private static double parseD(String s) {
        if (s == null || s.isBlank()) return 0.0;
        return Double.parseDouble(s.trim().replace(",", "."));
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s.trim());
    }

    // ---------- Getters / setters (necessaires pour la liaison Thymeleaf th:field) ----------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getAltitude() { return altitude; }
    public void setAltitude(String altitude) { this.altitude = altitude; }

    public String getTypeSupport() { return typeSupport; }
    public void setTypeSupport(String typeSupport) { this.typeSupport = typeSupport; }

    public String getHauteurPylone() { return hauteurPylone; }
    public void setHauteurPylone(String hauteurPylone) { this.hauteurPylone = hauteurPylone; }

    public String getNombreAntennes() { return nombreAntennes; }
    public void setNombreAntennes(String nombreAntennes) { this.nombreAntennes = nombreAntennes; }

    public String getHba() { return hba; }
    public void setHba(String hba) { this.hba = hba; }

    public String getHauteurAntenneSurPylone() { return hauteurAntenneSurPylone; }
    public void setHauteurAntenneSurPylone(String v) { this.hauteurAntenneSurPylone = v; }

    public String getAzimut() { return azimut; }
    public void setAzimut(String azimut) { this.azimut = azimut; }

    public String getTiltMecanique() { return tiltMecanique; }
    public void setTiltMecanique(String tiltMecanique) { this.tiltMecanique = tiltMecanique; }

    public String getTiltElectrique() { return tiltElectrique; }
    public void setTiltElectrique(String tiltElectrique) { this.tiltElectrique = tiltElectrique; }

    public String getGainAntenne() { return gainAntenne; }
    public void setGainAntenne(String gainAntenne) { this.gainAntenne = gainAntenne; }

    public String getPolarisation() { return polarisation; }
    public void setPolarisation(String polarisation) { this.polarisation = polarisation; }

    public String getBandeFrequence() { return bandeFrequence; }
    public void setBandeFrequence(String bandeFrequence) { this.bandeFrequence = bandeFrequence; }

    public String getLargeurBande() { return largeurBande; }
    public void setLargeurBande(String largeurBande) { this.largeurBande = largeurBande; }

    public String getPuissanceEmission() { return puissanceEmission; }
    public void setPuissanceEmission(String puissanceEmission) { this.puissanceEmission = puissanceEmission; }

    public String getTypeEquipementRadio() { return typeEquipementRadio; }
    public void setTypeEquipementRadio(String v) { this.typeEquipementRadio = v; }

    public String getRayonCouverture() { return rayonCouverture; }
    public void setRayonCouverture(String rayonCouverture) { this.rayonCouverture = rayonCouverture; }

    public String getTypeAlimentation() { return typeAlimentation; }
    public void setTypeAlimentation(String typeAlimentation) { this.typeAlimentation = typeAlimentation; }

    public boolean isPresenceOnduleur() { return presenceOnduleur; }
    public void setPresenceOnduleur(boolean presenceOnduleur) { this.presenceOnduleur = presenceOnduleur; }

    public boolean isPresencePanneauxSolaires() { return presencePanneauxSolaires; }
    public void setPresencePanneauxSolaires(boolean v) { this.presencePanneauxSolaires = v; }

    public String getDateMiseEnService() { return dateMiseEnService; }
    public void setDateMiseEnService(String d) { this.dateMiseEnService = d; }

    public String getDateDerniereMaintenance() { return dateDerniereMaintenance; }
    public void setDateDerniereMaintenance(String d) { this.dateDerniereMaintenance = d; }

    public String getDateDerniereMiseAJour() { return dateDerniereMiseAJour; }
    public void setDateDerniereMiseAJour(String d) { this.dateDerniereMiseAJour = d; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public String getNouvelleEntreeHistorique() { return nouvelleEntreeHistorique; }
    public void setNouvelleEntreeHistorique(String v) { this.nouvelleEntreeHistorique = v; }
}
