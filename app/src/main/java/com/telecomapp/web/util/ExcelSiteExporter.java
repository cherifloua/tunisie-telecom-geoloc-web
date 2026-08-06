package com.telecomapp.web.util;

import com.telecomapp.web.model.Site;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Genere un fichier Excel (.xlsx) rempli avec tous les sites actuellement
 * enregistres (memes intitules de colonnes que le modele d'import, voir
 * ExcelTemplateGenerator), pour telechargement depuis la liste des sites.
 *
 * N'ajoute qu'une nouvelle fonctionnalite (export) : ne modifie ni le modele
 * vierge, ni l'import, ni aucune autre partie de l'application.
 */
public final class ExcelSiteExporter {

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

    private ExcelSiteExporter() {
    }

    public static byte[] export(List<Site> sites) throws IOException {
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

            int r = 1;
            for (Site s : sites) {
                Row row = sheet.createRow(r++);
                int c = 0;
                row.createCell(c++).setCellValue(s.getNom());
                row.createCell(c++).setCellValue(s.getType());
                row.createCell(c++).setCellValue(s.getEtat());
                row.createCell(c++).setCellValue(s.getRegion());
                row.createCell(c++).setCellValue(s.getAdresse());
                row.createCell(c++).setCellValue(s.getLatitude());
                row.createCell(c++).setCellValue(s.getLongitude());
                row.createCell(c++).setCellValue(s.getAltitude());
                row.createCell(c++).setCellValue(s.getTypeSupport());
                row.createCell(c++).setCellValue(s.getHauteurPylone());
                row.createCell(c++).setCellValue(s.getNombreAntennes());
                row.createCell(c++).setCellValue(s.getHba());
                row.createCell(c++).setCellValue(s.getHauteurAntenneSurPylone());
                row.createCell(c++).setCellValue(s.getAzimut());
                row.createCell(c++).setCellValue(s.getTiltMecanique());
                row.createCell(c++).setCellValue(s.getTiltElectrique());
                row.createCell(c++).setCellValue(s.getGainAntenne());
                row.createCell(c++).setCellValue(s.getPolarisation());
                row.createCell(c++).setCellValue(s.getBandeFrequence());
                row.createCell(c++).setCellValue(s.getLargeurBande());
                row.createCell(c++).setCellValue(s.getPuissanceEmission());
                row.createCell(c++).setCellValue(s.getTypeEquipementRadio());
                row.createCell(c++).setCellValue(s.getRayonCouverture());
                row.createCell(c++).setCellValue(s.getTypeAlimentation());
                row.createCell(c++).setCellValue(s.isPresenceOnduleur() ? "Oui" : "Non");
                row.createCell(c++).setCellValue(s.isPresencePanneauxSolaires() ? "Oui" : "Non");
                row.createCell(c++).setCellValue(s.getDateMiseEnService() == null ? "" : s.getDateMiseEnService().toString());
                row.createCell(c++).setCellValue(s.getDateDerniereMaintenance() == null ? "" : s.getDateDerniereMaintenance().toString());
                row.createCell(c++).setCellValue(s.getDateDerniereMiseAJour() == null ? "" : s.getDateDerniereMiseAJour().toString());
                row.createCell(c++).setCellValue(s.getObservations());
                row.createCell(c++).setCellValue(s.getCellule());
                row.createCell(c++).setCellValue(s.getCanal());
                row.createCell(c++).setCellValue(s.getCid());
                row.createCell(c++).setCellValue(s.getPci());
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
