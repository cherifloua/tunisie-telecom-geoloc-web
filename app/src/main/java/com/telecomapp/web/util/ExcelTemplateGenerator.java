package com.telecomapp.web.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Genere un fichier Excel (.xlsx) vierge avec les bons intitules de colonnes,
 * pour que l'utilisateur sache exactement quoi remplir avant d'importer ses
 * sites (voir ExcelSiteImporter pour la liste des en-tetes reconnus).
 */
public final class ExcelTemplateGenerator {

    private static final String[] HEADERS = {
            "Nom", "Technologie", "Etat", "Region", "Adresse", "Latitude", "Longitude", "Altitude",
            "Type de support", "Hauteur du pylone", "Nombre d'antennes",
            "HBA", "Hauteur antenne sur pylone", "Azimut", "Tilt mecanique", "Tilt electrique",
            "Gain antenne", "Polarisation",
            "Bande de frequence", "Largeur de bande", "Puissance d'emission",
            "Type d'equipement radio", "Rayon de couverture",
            "Type d'alimentation", "Onduleur", "Panneaux solaires",
            "Date mise en service", "Date derniere maintenance", "Date derniere mise a jour",
            "Observations",
            "Cellule", "Canal (BCCH/ARFCN)", "CID", "PCI"
    };

    private static final String[] EXAMPLE_ROW = {
            "Antenne Exemple", "4G", "Actif", "Kairouan", "Avenue Habib Bourguiba", "35.6789", "10.0965", "65",
            "Pylone autoportant", "30", "3",
            "95", "28", "120", "2", "4",
            "17.5", "+/-45",
            "1800", "20", "43",
            "eNodeB", "5",
            "Reseau electrique", "Oui", "Non",
            "2024-01-15", "2026-05-01", "2026-05-01",
            "Exemple a remplacer / supprimer",
            "LKA001O", "6200", "41701", "198"
    };

    private ExcelTemplateGenerator() {
    }

    public static byte[] generate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sites");

            CellStyle headerStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            headerStyle.setFont(boldFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 22 * 256);
            }

            Row exampleRow = sheet.createRow(1);
            for (int i = 0; i < EXAMPLE_ROW.length; i++) {
                exampleRow.createCell(i).setCellValue(EXAMPLE_ROW[i]);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
