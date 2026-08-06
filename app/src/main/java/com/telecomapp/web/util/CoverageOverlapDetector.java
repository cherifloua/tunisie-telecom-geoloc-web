package com.telecomapp.web.util;

import com.telecomapp.web.model.Site;

import java.util.ArrayList;
import java.util.List;

/**
 * Detecte les chevauchements de couverture entre secteurs d'antennes.
 *
 * Le calcul se base uniquement sur des donnees reelles du site (aucune
 * valeur d'affichage/agrandissement visuel n'intervient ici) :
 *  - la distance reelle entre les deux sites (formule de haversine) ;
 *  - l'azimut de chaque antenne (direction du faisceau, 0-360 deg) ;
 *  - le rayon de couverture estime de chaque antenne (rayonCouverture, km).
 *
 * Deux secteurs sont consideres en chevauchement si le site A se trouve
 * dans le cone de couverture du site B (ou inversement) : la distance entre
 * les deux sites est inferieure ou egale au rayon de couverture de
 * l'antenne emettrice, ET le site recepteur se trouve dans la largeur de
 * faisceau de l'antenne emettrice. C'est la meme largeur de faisceau (65
 * deg) que celle utilisee pour dessiner les secteurs sur la carte (map.html),
 * afin que l'alerte corresponde a ce que l'utilisateur voit visuellement.
 */
public final class CoverageOverlapDetector {

    /** Largeur de faisceau typique d'une antenne sectorielle (meme valeur que sur la carte). */
    public static final double BEAM_WIDTH_DEG = 65.0;

    private static final double EARTH_RADIUS_KM = 6371.0;

    private CoverageOverlapDetector() {
    }

    /** Un chevauchement detecte entre deux sites, avec la distance reelle qui les separe. */
    public record Overlap(Site siteA, Site siteB, double distanceKm) {

        public double distanceArrondie() {
            return Math.round(distanceKm * 10) / 10.0;
        }
    }

    /** Analyse toutes les paires de sites et renvoie celles dont les secteurs se chevauchent. */
    public static List<Overlap> detect(List<Site> sites) {
        List<Overlap> overlaps = new ArrayList<>();
        for (int i = 0; i < sites.size(); i++) {
            for (int j = i + 1; j < sites.size(); j++) {
                Site a = sites.get(i);
                Site b = sites.get(j);
                double distanceKm = haversineKm(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
                if (isOverlap(a, b, distanceKm)) {
                    overlaps.add(new Overlap(a, b, distanceKm));
                }
            }
        }
        return overlaps;
    }

    private static boolean isOverlap(Site a, Site b, double distanceKm) {
        double radiusA = Math.max(a.getRayonCouverture(), 0);
        double radiusB = Math.max(b.getRayonCouverture(), 0);
        if (radiusA <= 0 && radiusB <= 0) {
            return false; // aucune couverture renseignee, rien a comparer
        }
        if (distanceKm > radiusA + radiusB) {
            return false; // sites trop eloignes : aucun risque de chevauchement
        }

        boolean aCouvreB = distanceKm <= radiusA && dansLeFaisceau(a.getAzimut(), bearingDeg(a, b));
        boolean bCouvreA = distanceKm <= radiusB && dansLeFaisceau(b.getAzimut(), bearingDeg(b, a));
        return aCouvreB || bCouvreA;
    }

    /** Le site vise (bearingDeg) est-il dans la largeur de faisceau de l'antenne (azimut) ? */
    private static boolean dansLeFaisceau(double azimutDeg, double bearingDeg) {
        return angularDifference(azimutDeg, bearingDeg) <= BEAM_WIDTH_DEG / 2.0;
    }

    /** Ecart angulaire (0-180 deg) entre deux caps, en tenant compte du passage 360 -> 0. */
    private static double angularDifference(double angleA, double angleB) {
        double diff = Math.abs(angleA - angleB) % 360;
        return diff > 180 ? 360 - diff : diff;
    }

    /** Cap (0-360 deg) du point "from" vers le point "to", formule de navigation spherique. */
    private static double bearingDeg(Site from, Site to) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double dLon = Math.toRadians(to.getLongitude() - from.getLongitude());
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double brng = Math.toDegrees(Math.atan2(y, x));
        return (brng + 360) % 360;
    }

    /** Distance reelle (km) entre deux coordonnees GPS (formule de haversine). */
    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double sinLat = Math.sin(dLat / 2);
        double sinLon = Math.sin(dLon / 2);
        double h = sinLat * sinLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinLon * sinLon;
        double c = 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
        return EARTH_RADIUS_KM * c;
    }
}
