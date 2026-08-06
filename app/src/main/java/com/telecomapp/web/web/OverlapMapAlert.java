package com.telecomapp.web.web;

import com.telecomapp.web.util.CoverageOverlapDetector;

/**
 * Represente un chevauchement de couverture pour l'affichage sur la carte
 * GPS (JavaScript, via Thymeleaf th:inline="javascript"). Ne contient que
 * des champs simples (int, String, double), pour les memes raisons que
 * SiteMapPoint : eviter tout champ non serialisable par Thymeleaf.
 */
public class OverlapMapAlert {
    public int idA;
    public String nomA;
    public int idB;
    public String nomB;
    public double distanceKm;

    public static OverlapMapAlert fromOverlap(CoverageOverlapDetector.Overlap o) {
        OverlapMapAlert a = new OverlapMapAlert();
        a.idA = o.siteA().getId();
        a.nomA = o.siteA().getNom();
        a.idB = o.siteB().getId();
        a.nomB = o.siteB().getNom();
        a.distanceKm = o.distanceArrondie();
        return a;
    }
}
