package com.telecomapp.web.util;

import com.telecomapp.web.model.Site;

import java.util.List;

/**
 * Generates KML (Google Earth format) content from a list of telecom sites.
 * Unlike the desktop version, this does not open Google Earth directly (there
 * is no "local machine" concept on a server) — instead the controller sends
 * the generated content as a file download that the browser (PC or phone)
 * saves, ready to be opened in Google Earth Pro / Google Earth Web.
 */
public class KmlExporter {

    public static String export(List<Site> sites) {
        StringBuilder out = new StringBuilder();
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        out.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
        out.append("<Document>\n");
        out.append("  <name>Sites Tunisie Telecom</name>\n");
        out.append("  <description><![CDATA[Icone/remplissage = technologie (2G gris, 3G bleu, 4G vert, 5G orange). "
                + "Contour du secteur = etat (vert=Actif, orange=Maintenance, rouge=En panne).]]></description>\n");

        for (Site s : sites) {
            out.append("  <Placemark>\n");
            out.append("    <name>").append(escape(s.getNom())).append("</name>\n");
            out.append("    <description><![CDATA[").append(buildDescription(s)).append("]]></description>\n");
            out.append(buildStyle(s));
            out.append("    <MultiGeometry>\n");
            out.append("      <Point>\n");
            out.append("        <coordinates>").append(s.getLongitude()).append(",").append(s.getLatitude())
                    .append(",").append(s.getAltitude()).append("</coordinates>\n");
            out.append("      </Point>\n");
            out.append(buildSectorPolygon(s));
            out.append("    </MultiGeometry>\n");
            out.append("  </Placemark>\n");
        }

        out.append("</Document>\n");
        out.append("</kml>\n");
        return out.toString();
    }

    private static final double BEAM_WIDTH_DEG = 65.0; // largeur de faisceau typique d'une antenne sectorielle
    private static final double MIN_RADIUS_KM = 0.5;   // rayon minimal pour rester visible meme si non renseigne
    private static final double SECTOR_RADIUS_SCALE = 2.5; // agrandit visuellement les secteurs (demande client)
    private static final double ICON_SCALE = 0.6;          // fleche plus petite (demande client)
    private static final int SECTOR_STEPS = 12;

    /**
     * Polygone "camembert" representant le secteur de couverture du site :
     * oriente selon l'azimut, ouvert sur BEAM_WIDTH_DEG degres, et etendu sur
     * le rayon de couverture estime (multiplie par SECTOR_RADIUS_SCALE pour
     * rester bien visible sur la carte). Superposer les KML de plusieurs
     * sites permet de reperer visuellement les zones de chevauchement.
     */
    private static String buildSectorPolygon(Site s) {
        double radiusKm = Math.max(s.getRayonCouverture(), MIN_RADIUS_KM) * SECTOR_RADIUS_SCALE;
        StringBuilder coords = new StringBuilder();
        coords.append(s.getLongitude()).append(",").append(s.getLatitude()).append(",0 ");
        for (int i = 0; i <= SECTOR_STEPS; i++) {
            double angle = s.getAzimut() - BEAM_WIDTH_DEG / 2.0 + (BEAM_WIDTH_DEG * i / SECTOR_STEPS);
            double[] p = destinationPoint(s.getLatitude(), s.getLongitude(), angle, radiusKm);
            coords.append(p[1]).append(",").append(p[0]).append(",0 ");
        }
        coords.append(s.getLongitude()).append(",").append(s.getLatitude()).append(",0");

        StringBuilder poly = new StringBuilder();
        poly.append("      <Polygon>\n");
        poly.append("        <extrude>0</extrude>\n");
        poly.append("        <altitudeMode>clampToGround</altitudeMode>\n");
        poly.append("        <outerBoundaryIs><LinearRing><coordinates>")
                .append(coords).append("</coordinates></LinearRing></outerBoundaryIs>\n");
        poly.append("      </Polygon>\n");
        return poly.toString();
    }

    /** Point situe a une distance (km) et un cap (deg) donnes depuis un point GPS (navigation spherique). */
    private static double[] destinationPoint(double lat, double lon, double bearingDeg, double distKm) {
        double r = 6371.0;
        double brng = Math.toRadians(bearingDeg);
        double lat1 = Math.toRadians(lat);
        double lon1 = Math.toRadians(lon);
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distKm / r)
                + Math.cos(lat1) * Math.sin(distKm / r) * Math.cos(brng));
        double lon2 = lon1 + Math.atan2(
                Math.sin(brng) * Math.sin(distKm / r) * Math.cos(lat1),
                Math.cos(distKm / r) - Math.sin(lat1) * Math.sin(lat2));
        return new double[]{Math.toDegrees(lat2), Math.toDegrees(lon2)};
    }

    /**
     * Style du Placemark :
     * - IconStyle : couleur selon la technologie (2G/3G/4G/5G), oriente (heading) selon l'azimut.
     * - LineStyle : couleur selon l'etat du site (vert=Actif, orange=Maintenance, rouge=En panne),
     *   applique au contour du secteur de couverture (buildSectorPolygon).
     * - PolyStyle : remplissage translucide selon la technologie, pour repérer la technologie
     *   d'un coup d'oeil tout en gardant le contour comme indicateur d'etat.
     */
    private static String buildStyle(Site s) {
        String techColor = toKmlColor(s.colorForType());
        String etatColor = toKmlColor(s.colorForEtat());
        String techColorTranslucent = "80" + techColor.substring(2); // meme couleur, alpha ~50%

        StringBuilder st = new StringBuilder();
        st.append("    <Style>\n");
        st.append("      <IconStyle>\n");
        st.append("        <color>").append(techColor).append("</color>\n");
        st.append("        <scale>").append(ICON_SCALE).append("</scale>\n");
        st.append("        <heading>").append(s.getAzimut()).append("</heading>\n");
        st.append("        <Icon><href>http://maps.google.com/mapfiles/kml/shapes/arrow.png</href></Icon>\n");
        st.append("      </IconStyle>\n");
        st.append("      <LineStyle>\n");
        st.append("        <color>").append(etatColor).append("</color>\n");
        st.append("        <width>3</width>\n");
        st.append("      </LineStyle>\n");
        st.append("      <PolyStyle>\n");
        st.append("        <color>").append(techColorTranslucent).append("</color>\n");
        st.append("      </PolyStyle>\n");
        st.append("    </Style>\n");
        return st.toString();
    }

    /** Convertit une couleur hex "#RRGGBB" (utilisee dans l'UI web) au format KML "aabbggrr". */
    private static String toKmlColor(String hexRgb) {
        if (hexRgb == null || !hexRgb.startsWith("#") || hexRgb.length() != 7) {
            return "ffffffff";
        }
        String rr = hexRgb.substring(1, 3);
        String gg = hexRgb.substring(3, 5);
        String bb = hexRgb.substring(5, 7);
        return "ff" + bb + gg + rr;
    }

    private static String buildDescription(Site s) {
        StringBuilder d = new StringBuilder();
        d.append("<b>Type :</b> ").append(s.getType()).append("<br/>");
        d.append("<b>Etat :</b> ").append(s.getEtat()).append("<br/>");
        d.append("<b>Region :</b> ").append(s.getRegion()).append("<br/>");
        d.append("<b>Adresse :</b> ").append(s.getAdresse()).append("<br/>");
        d.append("<b>Altitude :</b> ").append(s.getAltitude()).append(" m<br/><br/>");

        d.append("<b>--- Infrastructure ---</b><br/>");
        d.append("Support : ").append(s.getTypeSupport()).append("<br/>");
        d.append("Hauteur pylone : ").append(s.getHauteurPylone()).append(" m<br/>");
        d.append("Nombre d'antennes : ").append(s.getNombreAntennes()).append("<br/><br/>");

        d.append("<b>--- Antenne ---</b><br/>");
        d.append("HBA : ").append(s.getHba()).append("<br/>");
        d.append("Hauteur antenne sur pylone : ").append(s.getHauteurAntenneSurPylone()).append(" m<br/>");
        d.append("Azimut : ").append(s.getAzimut()).append(" deg<br/>");
        d.append("Tilt mecanique : ").append(s.getTiltMecanique()).append(" deg<br/>");
        d.append("Tilt electrique : ").append(s.getTiltElectrique()).append(" deg<br/>");
        d.append("Gain antenne : ").append(s.getGainAntenne()).append(" dBi<br/>");
        d.append("Polarisation : ").append(s.getPolarisation()).append("<br/><br/>");

        d.append("<b>--- Radio ---</b><br/>");
        d.append("Bande de frequence : ").append(s.getBandeFrequence()).append(" MHz<br/>");
        d.append("Largeur de bande : ").append(s.getLargeurBande()).append(" MHz<br/>");
        d.append("Puissance d'emission : ").append(s.getPuissanceEmission()).append(" dBm<br/>");
        d.append("Equipement radio : ").append(s.getTypeEquipementRadio()).append("<br/>");
        d.append("Rayon de couverture estime : ").append(s.getRayonCouverture()).append(" km<br/><br/>");

        d.append("<b>--- Energie ---</b><br/>");
        d.append("Alimentation : ").append(s.getTypeAlimentation()).append("<br/>");
        d.append("Onduleur : ").append(s.isPresenceOnduleur() ? "Oui" : "Non").append("<br/>");
        d.append("Panneaux solaires : ").append(s.isPresencePanneauxSolaires() ? "Oui" : "Non").append("<br/><br/>");

        d.append("<b>--- Exploitation ---</b><br/>");
        d.append("Mise en service : ").append(s.getDateMiseEnService() == null ? "-" : s.getDateMiseEnService()).append("<br/>");
        d.append("Derniere maintenance : ").append(s.getDateDerniereMaintenance() == null ? "-" : s.getDateDerniereMaintenance()).append("<br/>");
        d.append("Derniere mise a jour : ").append(s.getDateDerniereMiseAJour() == null ? "-" : s.getDateDerniereMiseAJour()).append("<br/>");
        d.append("Observations : ").append(s.getObservations() == null ? "" : s.getObservations());

        return d.toString();
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
