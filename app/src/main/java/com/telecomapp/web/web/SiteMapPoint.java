package com.telecomapp.web.web;

import com.telecomapp.web.model.Site;

/**
 * Represente un site pour l'affichage sur la carte GPS (JavaScript, via
 * Thymeleaf th:inline="javascript"). Ne contient volontairement que des
 * champs simples (String, double) : le serialiseur JSON interne de
 * Thymeleaf ne sait pas convertir les champs LocalDate presents sur Site
 * (dateMiseEnService, dateDerniereMaintenance, dateDerniereMiseAJour), ce
 * qui provoquait une erreur 500 en plein rendu de la page carte.
 */
public class SiteMapPoint {
    public int id;
    public String nom;
    public String type;
    public String etat;
    public String adresse;
    public double latitude;
    public double longitude;
    public double azimut;
    public double rayonCouverture;

    public static SiteMapPoint fromSite(Site s) {
        SiteMapPoint p = new SiteMapPoint();
        p.id = s.getId();
        p.nom = s.getNom();
        p.type = s.getType();
        p.etat = s.getEtat();
        p.adresse = s.getAdresse();
        p.latitude = s.getLatitude();
        p.longitude = s.getLongitude();
        p.azimut = s.getAzimut();
        p.rayonCouverture = s.getRayonCouverture();
        return p;
    }
}
