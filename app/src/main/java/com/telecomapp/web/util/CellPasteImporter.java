package com.telecomapp.web.util;

import com.telecomapp.web.model.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Permet de coller directement une liste de cellules (une par ligne, ou meme
 * toutes a la suite separees par des espaces) au format :
 *
 *   SYSTEM;SITE;LAT;LON;CELL;CH;CID;PCI;DIR
 *
 * Exemple :
 *   LTE;B4G3G_OULED_BELLIL;35.835395;9.999891;LKA001O;6200;41701;198;10
 *
 * Chaque ligne devient un site (une cellule = un site, comme pour l'import
 * Excel). Les colonnes sont reparties ainsi :
 *   SYSTEM -> Technologie, SITE -> Nom, LAT/LON -> Latitude/Longitude,
 *   CELL -> Cellule, CH -> Canal (BCCH/ARFCN), CID -> CID, PCI -> PCI,
 *   DIR -> Azimut.
 *
 * Cette classe ne modifie aucune fonctionnalite existante : c'est une nouvelle
 * facon d'alimenter la liste et le fichier Excel, en plus du formulaire et de
 * l'import Excel.
 */
public final class CellPasteImporter {

    /** En-tete optionnelle reconnue et ignoree si presente en premiere ligne. */
    private static final String HEADER_PREFIX = "system;site";

    public static final class Result {
        public final List<Site> sites = new ArrayList<>();
        public final List<String> errors = new ArrayList<>();
    }

    private CellPasteImporter() {
    }

    public static Result parse(String raw) {
        Result result = new Result();
        if (raw == null || raw.isBlank()) {
            result.errors.add("Aucune donnee collee.");
            return result;
        }

        // Un enregistrement ne contient pas d'espace : on peut donc decouper
        // sur n'importe quel espace/retour a la ligne, que l'utilisateur ait
        // colle une ligne par cellule ou tout a la suite.
        String[] tokens = raw.trim().split("\\s+");

        int lineNumber = 0;
        for (String token : tokens) {
            if (token.isBlank()) continue;
            lineNumber++;

            String cleaned = token.trim();
            if (cleaned.toLowerCase(Locale.ROOT).startsWith(HEADER_PREFIX)) {
                continue; // ligne d'en-tete : ignoree
            }

            try {
                Site site = parseToken(cleaned);
                result.sites.add(site);
            } catch (Exception ex) {
                result.errors.add("Entree " + lineNumber + " (\"" + cleaned + "\") : " + ex.getMessage());
            }
        }
        return result;
    }

    private static Site parseToken(String token) {
        String[] p = token.split(";", -1);
        if (p.length < 4) {
            throw new IllegalArgumentException("format attendu SYSTEM;SITE;LAT;LON;CELL;CH;CID;PCI;DIR");
        }

        String system = get(p, 0).isBlank() ? "4G" : get(p, 0);
        String siteName = get(p, 1);
        if (siteName.isBlank()) {
            throw new IllegalArgumentException("le nom du site (SITE) est obligatoire");
        }
        double lat = parseDouble(get(p, 2), "LAT");
        double lon = parseDouble(get(p, 3), "LON");
        String cell = get(p, 4);
        String ch = get(p, 5);
        String cid = get(p, 6);
        String pci = get(p, 7);
        double dir = p.length > 8 && !get(p, 8).isBlank() ? parseDouble(get(p, 8), "DIR") : 0.0;

        Site site = new Site(0, siteName, normalizeTechnologie(system), lat, lon,
                "", "Actif",
                "", 0.0,
                "Pylone autoportant", 0.0, 1,
                0.0, 0.0, dir, 0.0,
                0.0, 0.0, "Verticale",
                "", 0.0, 0.0,
                "", 0.0,
                "Reseau electrique", false, false,
                null, null, null, "");
        site.setCellule(cell);
        site.setCanal(ch);
        site.setCid(cid);
        site.setPci(pci);
        return site;
    }

    /** Le systeme colle (ex: LTE, GSM, UMTS) est ramene aux valeurs de technologie utilisees ailleurs (2G/3G/4G/5G). */
    private static String normalizeTechnologie(String system) {
        String s = system.trim().toUpperCase(Locale.ROOT);
        return switch (s) {
            case "GSM", "2G" -> "2G";
            case "UMTS", "WCDMA", "3G" -> "3G";
            case "LTE", "4G" -> "4G";
            case "NR", "5G" -> "5G";
            default -> system.trim().isEmpty() ? "4G" : system.trim();
        };
    }

    private static String get(String[] p, int idx) {
        return idx < p.length && p[idx] != null ? p[idx].trim() : "";
    }

    private static double parseDouble(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("le champ " + field + " est obligatoire et doit etre un nombre");
        }
        try {
            return Double.parseDouble(raw.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("valeur numerique invalide pour " + field + " : " + raw);
        }
    }
}
