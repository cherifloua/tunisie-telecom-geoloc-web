package com.telecomapp.web.model;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Simple local-file persistence layer for Site objects.
 * Stores data as a semicolon-separated CSV file (data/sites.csv), so the
 * application runs without needing to install or configure a database server.
 *
 * Registered as a singleton Spring bean: since several users can now access
 * the app at the same time over the web, the public methods are synchronized
 * to avoid concurrent writes corrupting the CSV file.
 */
@Component
public class SiteRepository {

    private static final Path DATA_DIR = Paths.get("data");
    private static final Path DATA_FILE = DATA_DIR.resolve("sites.csv");

    private final List<Site> sites = new ArrayList<>();
    private int nextId = 1;

    public SiteRepository() {
        load();
        if (sites.isEmpty()) {
            seedSampleData();
        }
    }

    public synchronized List<Site> findAll() {
        return new ArrayList<>(sites);
    }

    public synchronized List<Site> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        if (k.isEmpty()) return findAll();
        List<Site> result = new ArrayList<>();
        for (Site s : sites) {
            if (s.getNom().toLowerCase().contains(k)
                    || s.getAdresse().toLowerCase().contains(k)
                    || s.getType().toLowerCase().contains(k)
                    || (s.getRegion() != null && s.getRegion().toLowerCase().contains(k))
                    || s.getCellule().toLowerCase().contains(k)
                    || s.getCanal().toLowerCase().contains(k)
                    || s.getCid().toLowerCase().contains(k)
                    || s.getPci().toLowerCase().contains(k)) {
                result.add(s);
            }
        }
        return result;
    }

    public synchronized void add(Site site) {
        site.setId(nextId++);
        sites.add(site);
        save();
    }

    public synchronized void update(Site updated) {
        for (int i = 0; i < sites.size(); i++) {
            if (sites.get(i).getId() == updated.getId()) {
                sites.set(i, updated);
                break;
            }
        }
        save();
    }

    public synchronized void delete(int id) {
        sites.removeIf(s -> s.getId() == id);
        save();
    }

    public synchronized Optional<Site> findById(int id) {
        return sites.stream().filter(s -> s.getId() == id).findFirst();
    }

    private void seedSampleData() {
        add(new Site(0, "Antenne Kairouan Nord", "4G", 35.6789, 10.0965, "Kairouan", "Actif",
                "Kairouan", 65.0,
                "Pylone autoportant", 30.0, 3,
                95.0, 28.0, 120.0, 2.0, 4.0, 17.5, "+/-45",
                "1800", 20.0, 43.0, "eNodeB", 5.0,
                "Reseau electrique", true, false,
                java.time.LocalDate.of(2020, 3, 15), java.time.LocalDate.of(2025, 11, 2),
                java.time.LocalDate.of(2025, 11, 2),
                "Site prioritaire, forte affluence en centre-ville"));
        add(new Site(0, "Centrale Telecom Sousse", "5G", 35.8256, 10.6084, "Sousse", "Actif",
                "Sousse", 12.0,
                "Rooftop", 18.0, 4,
                40.0, 20.0, 200.0, 0.0, 6.0, 20.0, "+/-45",
                "3500", 100.0, 40.0, "gNodeB", 2.5,
                "Reseau electrique", true, true,
                java.time.LocalDate.of(2022, 6, 1), java.time.LocalDate.of(2026, 1, 10),
                java.time.LocalDate.of(2026, 1, 10),
                "Zone touristique, deploiement 5G recent"));
        add(new Site(0, "Antenne Tunis Centre", "3G", 36.8065, 10.1815, "Tunis", "En panne",
                "Tunis", 25.0,
                "Pylone haubane", 40.0, 2,
                70.0, 32.0, 45.0, 3.0, 2.0, 16.0, "Verticale",
                "2100", 15.0, 41.0, "Node B", 3.0,
                "Groupe electrogene", false, false,
                java.time.LocalDate.of(2015, 9, 20), java.time.LocalDate.of(2026, 5, 18),
                java.time.LocalDate.of(2026, 5, 18),
                "Panne signalee : coupure d'alimentation, intervention prevue"));
        add(new Site(0, "Antenne Sfax Sud", "2G", 34.7406, 10.7603, "Sfax", "Actif",
                "Sfax", 8.0,
                "Monopole", 25.0, 1,
                50.0, 22.0, 300.0, 1.0, 0.0, 14.0, "Horizontale",
                "900", 10.0, 39.0, "BTS", 6.0,
                "Batterie", false, true,
                java.time.LocalDate.of(2010, 1, 5), java.time.LocalDate.of(2025, 8, 30),
                java.time.LocalDate.of(2025, 8, 30),
                "Site rural, couverture etendue"));
    }

    private void load() {
        try {
            if (!Files.exists(DATA_FILE)) return;
            List<String> lines = Files.readAllLines(DATA_FILE);
            for (String line : lines) {
                if (line.isBlank()) continue;
                Site s = Site.fromCsvLine(line);
                sites.add(s);
                nextId = Math.max(nextId, s.getId() + 1);
            }
        } catch (IOException e) {
            System.err.println("Impossible de charger les sites : " + e.getMessage());
        }
    }

    private void save() {
        try {
            Files.createDirectories(DATA_DIR);
            List<String> lines = new ArrayList<>();
            for (Site s : sites) lines.add(s.toCsvLine());
            Files.write(DATA_FILE, lines);
        } catch (IOException e) {
            System.err.println("Impossible d'enregistrer les sites : " + e.getMessage());
        }
    }
}
