package com.telecomapp.web.util;

import com.telecomapp.web.model.Site;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importe des sites telecom en masse a partir d'un fichier Excel (.xlsx/.xls).
 *
 * Format attendu : une premiere ligne d'en-tetes (les intitules ci-dessous,
 * dans n'importe quel ordre - seuls Nom, Latitude et Longitude sont
 * obligatoires), puis une ligne par site.
 *
 * En-tetes reconnus (insensibles a la casse/accents) :
 * Nom, Technologie, Etat, Region, Adresse, Latitude, Longitude, Altitude,
 * Type de support, Hauteur du pylone, Nombre d'antennes,
 * HBA, Hauteur antenne sur pylone, Azimut, Tilt mecanique, Tilt electrique,
 * Gain antenne, Polarisation, Bande de frequence, Largeur de bande,
 * Puissance d'emission, Type d'equipement radio, Rayon de couverture,
 * Type d'alimentation, Onduleur, Panneaux solaires,
 * Date mise en service, Date derniere maintenance, Date derniere mise a jour,
 * Observations.
 *
 * Ce fichier ne touche a aucune autre fonctionnalite : il ne fait qu'ajouter
 * une nouvelle facon de creer des sites, en plus du formulaire existant.
 */
public final class ExcelSiteImporter {

    /** Resultat d'un import : sites valides + erreurs eventuelles (numero de ligne -> message). */
    public static final class Result {
        public final List<Site> sites = new ArrayList<>();
        public final List<String> errors = new ArrayList<>();
    }

    private ExcelSiteImporter() {
    }

    public static Result importFrom(InputStream inputStream) throws IOException {
        Result result = new Result();
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                result.errors.add("Le fichier est vide.");
                return result;
            }
            Map<String, Integer> columns = new HashMap<>();
            for (Cell cell : headerRow) {
                String key = normalize(formatter.formatCellValue(cell));
                if (!key.isEmpty()) {
                    columns.put(key, cell.getColumnIndex());
                }
            }

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowBlank(row, formatter)) continue;

                int excelLineNumber = r + 1;
                try {
                    Site site = parseRow(row, columns, formatter);
                    result.sites.add(site);
                } catch (Exception ex) {
                    result.errors.add("Ligne " + excelLineNumber + " : " + ex.getMessage());
                }
            }
        }
        return result;
    }

    private static Site parseRow(Row row, Map<String, Integer> columns, DataFormatter formatter) {
        String nom = text(row, columns, formatter, "nom");
        String type = orDefault(text(row, columns, formatter, "technologie"), "4G");
        String etat = orDefault(text(row, columns, formatter, "etat"), "Actif");
        String region = text(row, columns, formatter, "region");
        String adresse = text(row, columns, formatter, "adresse");
        double latitude = number(row, columns, formatter, "latitude", true);
        double longitude = number(row, columns, formatter, "longitude", true);
        double altitude = number(row, columns, formatter, "altitude", false);

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("le nom du site est obligatoire");
        }

        String typeSupport = orDefault(text(row, columns, formatter, "typedesupport"), "Pylone autoportant");
        double hauteurPylone = number(row, columns, formatter, "hauteurdupylone", false);
        int nombreAntennes = (int) number(row, columns, formatter, "nombredantennes", false);

        double hba = number(row, columns, formatter, "hba", false);
        double hauteurAntenneSurPylone = number(row, columns, formatter, "hauteurantennesurpylone", false);
        double azimut = number(row, columns, formatter, "azimut", false);
        double tiltMecanique = number(row, columns, formatter, "tiltmecanique", false);
        double tiltElectrique = number(row, columns, formatter, "tiltelectrique", false);
        double gainAntenne = number(row, columns, formatter, "gainantenne", false);
        String polarisation = orDefault(text(row, columns, formatter, "polarisation"), "+/-45");

        String bandeFrequence = text(row, columns, formatter, "bandedefrequence");
        double largeurBande = number(row, columns, formatter, "largeurdebande", false);
        double puissanceEmission = number(row, columns, formatter, "puissancedemission", false);
        String typeEquipementRadio = text(row, columns, formatter, "typedequipementradio");
        double rayonCouverture = number(row, columns, formatter, "rayondecouverture", false);

        String typeAlimentation = orDefault(text(row, columns, formatter, "typedalimentation"), "Reseau electrique");
        boolean presenceOnduleur = bool(row, columns, formatter, "onduleur");
        boolean presencePanneauxSolaires = bool(row, columns, formatter, "panneauxsolaires");

        LocalDate dateMiseEnService = date(row, columns, formatter, "datemiseenservice");
        LocalDate dateDerniereMaintenance = date(row, columns, formatter, "datedernieremaintenance");
        LocalDate dateDerniereMiseAJour = date(row, columns, formatter, "datedernieremiseajour");
        String observations = orDefault(text(row, columns, formatter, "observations"), "");

        return new Site(0, nom.trim(), type, latitude, longitude,
                orDefault(adresse, "").trim(), etat,
                orDefault(region, "").trim(), altitude,
                typeSupport, hauteurPylone, nombreAntennes,
                hba, hauteurAntenneSurPylone, azimut, tiltMecanique,
                tiltElectrique, gainAntenne, polarisation,
                orDefault(bandeFrequence, "").trim(), largeurBande, puissanceEmission,
                orDefault(typeEquipementRadio, "").trim(), rayonCouverture,
                typeAlimentation, presenceOnduleur, presencePanneauxSolaires,
                dateMiseEnService, dateDerniereMaintenance, dateDerniereMiseAJour, observations.trim());
    }

    // ---------- Lecture de cellules ----------

    private static String text(Row row, Map<String, Integer> columns, DataFormatter formatter, String header) {
        Integer idx = columns.get(header);
        if (idx == null) return "";
        Cell cell = row.getCell(idx);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private static double number(Row row, Map<String, Integer> columns, DataFormatter formatter, String header, boolean required) {
        String raw = text(row, columns, formatter, header);
        if (raw.isBlank()) {
            if (required) {
                throw new IllegalArgumentException("le champ '" + header + "' est obligatoire et doit etre un nombre");
            }
            return 0.0;
        }
        try {
            return Double.parseDouble(raw.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("valeur numerique invalide pour '" + header + "' : " + raw);
        }
    }

    private static boolean bool(Row row, Map<String, Integer> columns, DataFormatter formatter, String header) {
        String raw = text(row, columns, formatter, header).toLowerCase();
        return raw.equals("oui") || raw.equals("true") || raw.equals("1") || raw.equals("yes") || raw.equals("x");
    }

    private static LocalDate date(Row row, Map<String, Integer> columns, DataFormatter formatter, String header) {
        String raw = text(row, columns, formatter, header);
        if (raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null; // date optionnelle et mal formatee : ignoree plutot que de bloquer tout l'import
        }
    }

    private static String orDefault(String value, String def) {
        return (value == null || value.isBlank()) ? def : value;
    }

    private static boolean isRowBlank(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).isBlank()) return false;
        }
        return true;
    }

    /** Normalise un intitule de colonne : minuscules, sans accents, sans espaces/apostrophes. */
    private static String normalize(String s) {
        if (s == null) return "";
        String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        return n;
    }
}
