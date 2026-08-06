package com.telecomapp.web.web;

import com.telecomapp.web.model.Site;
import com.telecomapp.web.model.SiteRepository;
import com.telecomapp.web.util.CellPasteImporter;
import com.telecomapp.web.util.CoverageOverlapDetector;
import com.telecomapp.web.util.ExcelSiteExporter;
import com.telecomapp.web.util.ExcelSiteImporter;
import com.telecomapp.web.util.ExcelTemplateGenerator;
import com.telecomapp.web.util.KmlExporter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SiteController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MAX_HISTORIQUE_ENTRIES = 30;

    private final SiteRepository repository;

    public SiteController(SiteRepository repository) {
        this.repository = repository;
    }

    /**
     * Ajoute une nouvelle entree en tete de l'historique (plus recente en
     * premier) et limite le nombre d'entrees conservees pour eviter que la
     * ligne CSV ne grossisse indefiniment.
     */
    private static String prependHistorique(String existing, String newEntry) {
        List<String> entries = new java.util.ArrayList<>();
        entries.add(newEntry);
        if (existing != null && !existing.isBlank()) {
            for (String e : existing.split("\\|")) {
                String trimmed = e.trim();
                if (!trimmed.isEmpty()) entries.add(trimmed);
            }
        }
        if (entries.size() > MAX_HISTORIQUE_ENTRIES) {
            entries = entries.subList(0, MAX_HISTORIQUE_ENTRIES);
        }
        return String.join(" | ", entries);
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Site> sites = repository.findAll();
        int total = sites.size();

        long actifs = sites.stream().filter(s -> "Actif".equalsIgnoreCase(s.getEtat())).count();
        long maintenance = sites.stream().filter(s -> "Maintenance".equalsIgnoreCase(s.getEtat())).count();
        long panne = sites.stream().filter(s -> "En panne".equalsIgnoreCase(s.getEtat())).count();

        // Repartition par technologie, dans un ordre fixe et lisible
        Map<String, Long> parTechnologie = new LinkedHashMap<>();
        for (String tech : new String[]{"2G", "3G", "4G", "5G"}) {
            long count = sites.stream().filter(s -> tech.equalsIgnoreCase(s.getType())).count();
            parTechnologie.put(tech, count);
        }
        long autreTech = total - parTechnologie.values().stream().mapToLong(Long::longValue).sum();
        if (autreTech > 0) parTechnologie.put("Autre", autreTech);

        long nbRegions = sites.stream()
                .map(Site::getRegion)
                .filter(r -> r != null && !r.isBlank())
                .distinct()
                .count();

        long alimentationSecours = sites.stream()
                .filter(s -> s.isPresenceOnduleur() || s.isPresencePanneauxSolaires())
                .count();

        LocalDate today = LocalDate.now();
        long maintenanceProche = sites.stream()
                .filter(s -> s.getDateDerniereMaintenance() != null)
                .filter(s -> ChronoUnit.DAYS.between(s.getDateDerniereMaintenance(), today) > 180)
                .count();

        List<Site> recents = sites.stream()
                .sorted(Comparator.comparing(
                        (Site s) -> s.getDateDerniereMiseAJour() == null ? LocalDate.MIN : s.getDateDerniereMiseAJour(),
                        Comparator.reverseOrder()))
                .limit(5)
                .toList();

        double tauxDisponibilite = total == 0 ? 0 : (actifs * 100.0 / total);

        List<CoverageOverlapDetector.Overlap> chevauchements = CoverageOverlapDetector.detect(sites);

        model.addAttribute("total", total);
        model.addAttribute("actifs", actifs);
        model.addAttribute("maintenance", maintenance);
        model.addAttribute("panne", panne);
        model.addAttribute("parTechnologie", parTechnologie);
        model.addAttribute("nbRegions", nbRegions);
        model.addAttribute("alimentationSecours", alimentationSecours);
        model.addAttribute("maintenanceProche", maintenanceProche);
        model.addAttribute("recents", recents);
        model.addAttribute("tauxDisponibilite", tauxDisponibilite);
        model.addAttribute("chevauchements", chevauchements);
        model.addAttribute("nbChevauchements", chevauchements.size());
        return "dashboard";
    }

    /**
     * Ouvre directement Google Earth Web, centre sur le barycentre de tous
     * les sites enregistres (au lieu de se contenter de proposer un
     * telechargement de fichier KML). Utilise par le bouton "Export Google
     * Earth" du tableau de bord et par le lien de la barre laterale.
     */
    @GetMapping("/earth")
    public String openEarth() {
        return "redirect:" + buildEarthWebUrl(repository.findAll());
    }

    /**
     * Construit une URL Google Earth Web centree sur le barycentre de tous
     * les sites (ou sur la Tunisie par defaut s'il n'y a aucun site), pour
     * ouvrir directement Google Earth dans le navigateur au lieu de se
     * contenter de proposer un telechargement de fichier.
     */
    private static String buildEarthWebUrl(List<Site> sites) {
        double lat = 34.0; // Tunisie, par defaut
        double lng = 9.5;
        if (sites != null && !sites.isEmpty()) {
            double sumLat = 0;
            double sumLng = 0;
            for (Site s : sites) {
                sumLat += s.getLatitude();
                sumLng += s.getLongitude();
            }
            lat = sumLat / sites.size();
            lng = sumLng / sites.size();
        }
        return "https://earth.google.com/web/@" + lat + "," + lng + ",0a,300000d,35y,0h,0t,0r";
    }

    @GetMapping("/sites")
    public String list(@RequestParam(required = false) String q, Model model) {
        List<Site> sites = (q == null || q.isBlank()) ? repository.findAll() : repository.search(q);
        model.addAttribute("sites", sites);
        model.addAttribute("q", q == null ? "" : q);
        return "sites/list";
    }

    /** Telecharge un fichier Excel rempli avec tous les sites actuellement dans la liste (respecte la recherche en cours). */
    @GetMapping("/sites/export/xlsx")
    public ResponseEntity<byte[]> exportXlsx(@RequestParam(required = false) String q) throws java.io.IOException {
        List<Site> sites = (q == null || q.isBlank()) ? repository.findAll() : repository.search(q);
        byte[] bytes = ExcelSiteExporter.export(sites);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sites_tunisie_telecom.xlsx\"")
                .body(bytes);
    }

    @GetMapping("/sites/{id}")
    public String detail(@PathVariable int id, Model model) {
        Site site = repository.findById(id).orElse(null);
        if (site == null) return "redirect:/sites";
        model.addAttribute("site", site);
        return "sites/detail";
    }

    /**
     * L'ajout de site se fait uniquement via l'import Excel (exigence du
     * client, pour eviter la saisie manuelle site par site). Le formulaire
     * (sites/form.html) reste utilise uniquement pour modifier un site existant.
     */
    @GetMapping("/sites/new")
    public String newForm() {
        return "redirect:/sites/import";
    }

    /**
     * Formulaire d'import en masse de sites depuis un fichier Excel
     * (.xlsx/.xls), en complement de l'ajout site par site.
     */
    @GetMapping("/sites/import")
    public String importForm() {
        return "sites/import";
    }

    /** Telecharge un modele Excel vierge avec les bons intitules de colonnes. */
    @GetMapping("/sites/import/template")
    public ResponseEntity<byte[]> importTemplate() throws java.io.IOException {
        byte[] bytes = ExcelTemplateGenerator.generate();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"modele_import_sites.xlsx\"")
                .body(bytes);
    }

    /** Traite le fichier Excel envoye et ajoute chaque ligne valide comme nouveau site. */
    @PostMapping("/sites/import")
    public String importExcel(@RequestParam("file") MultipartFile file, Model model) {
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "Merci de choisir un fichier Excel (.xlsx ou .xls) a importer.");
            return "sites/import";
        }
        try {
            ExcelSiteImporter.Result result = ExcelSiteImporter.importFrom(file.getInputStream());
            String today = LocalDate.now().format(DATE_FMT);
            for (Site site : result.sites) {
                site.setDateDerniereMiseAJour(LocalDate.now());
                site.setHistorique(today + " : site importe depuis Excel");
                repository.add(site);
            }
            model.addAttribute("imported", result.sites.size());
            model.addAttribute("errors", result.errors);
            return "sites/import";
        } catch (Exception ex) {
            model.addAttribute("error", "Impossible de lire ce fichier Excel : " + ex.getMessage());
            return "sites/import";
        }
    }

    /**
     * Ajoute des sites/cellules a partir d'un texte colle directement au
     * format "SYSTEM;SITE;LAT;LON;CELL;CH;CID;PCI;DIR" (une ligne par
     * cellule). Complement de l'import Excel, sans rien modifier a celui-ci.
     */
    @PostMapping("/sites/import/paste")
    public String importPaste(@RequestParam(name = "data", required = false) String data, Model model) {
        CellPasteImporter.Result result = CellPasteImporter.parse(data);
        String today = LocalDate.now().format(DATE_FMT);
        for (Site site : result.sites) {
            site.setDateDerniereMiseAJour(LocalDate.now());
            site.setHistorique(today + " : site importe depuis un collage rapide");
            repository.add(site);
        }
        model.addAttribute("imported", result.sites.size());
        model.addAttribute("errors", result.errors);
        return "sites/import";
    }

    @GetMapping("/sites/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Site site = repository.findById(id).orElse(null);
        if (site == null) return "redirect:/sites";
        model.addAttribute("form", SiteFormData.fromSite(site));
        model.addAttribute("isEdit", true);
        model.addAttribute("lastUpdateLabel", site.getDateDerniereMiseAJour() == null
                ? "-" : site.getDateDerniereMiseAJour().format(DATE_FMT));
        model.addAttribute("historiqueEntries", site.historiqueEntries());
        return "sites/form";
    }

    @PostMapping("/sites/save")
    public String save(@ModelAttribute("form") SiteFormData form, Model model, RedirectAttributes redirectAttributes) {
        boolean isEdit = form.getId() > 0;
        try {
            Site site = form.toSite();
            String today = LocalDate.now().format(DATE_FMT);
            site.setDateDerniereMiseAJour(LocalDate.now());

            String noteSaisie = form.getNouvelleEntreeHistorique() == null ? "" : form.getNouvelleEntreeHistorique().trim();

            if (isEdit) {
                Site existing = repository.findById(site.getId()).orElse(null);
                String texte = noteSaisie.isEmpty() ? "site modifie" : noteSaisie;
                if (existing != null && !existing.getEtat().equalsIgnoreCase(site.getEtat())) {
                    texte += " (statut : " + existing.getEtat() + " -> " + site.getEtat() + ")";
                }
                String entry = today + " : " + texte;
                String previousHistorique = existing != null ? existing.getHistorique() : "";
                site.setHistorique(prependHistorique(previousHistorique, entry));
                repository.update(site);
                redirectAttributes.addFlashAttribute("message", "Site \"" + site.getNom() + "\" modifie avec succes.");
            } else {
                String texte = noteSaisie.isEmpty() ? "site cree" : noteSaisie;
                site.setHistorique(today + " : " + texte);
                repository.add(site);
                redirectAttributes.addFlashAttribute("message", "Site \"" + site.getNom() + "\" ajoute avec succes.");
            }
            return "redirect:/sites";
        } catch (Exception ex) {
            model.addAttribute("error", "Verifiez les champs (nom, latitude, longitude obligatoires ; "
                    + "hauteurs, angles, puissance et dates au format AAAA-MM-JJ doivent etre numeriques) : "
                    + ex.getMessage());
            model.addAttribute("isEdit", isEdit);
            if (isEdit) {
                Site existing = repository.findById(form.getId()).orElse(null);
                model.addAttribute("lastUpdateLabel", existing != null && existing.getDateDerniereMiseAJour() != null
                        ? existing.getDateDerniereMiseAJour().format(DATE_FMT) : "-");
                model.addAttribute("historiqueEntries", existing != null ? existing.historiqueEntries() : List.of());
            }
            return "sites/form";
        }
    }

    @PostMapping("/sites/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        repository.delete(id);
        redirectAttributes.addFlashAttribute("message", "Site supprime avec succes.");
        return "redirect:/sites";
    }

    @PostMapping("/sites/delete-bulk")
    public String deleteBulk(@RequestParam(name = "ids", required = false) List<Integer> ids,
                              RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Aucun site selectionne.");
            return "redirect:/sites";
        }
        for (int id : ids) {
            repository.delete(id);
        }
        redirectAttributes.addFlashAttribute("message", ids.size() + " site(s) supprime(s) avec succes.");
        return "redirect:/sites";
    }

    @GetMapping("/map")
    public String map(Model model) {
        try {
            List<Site> sites = repository.findAll();
            List<SiteMapPoint> points = sites.stream()
                    .map(SiteMapPoint::fromSite)
                    .toList();
            List<OverlapMapAlert> overlaps = CoverageOverlapDetector.detect(sites).stream()
                    .map(OverlapMapAlert::fromOverlap)
                    .toList();
            model.addAttribute("sites", points);
            model.addAttribute("overlapsJson", overlaps);
            model.addAttribute("mapError", null);
        } catch (Exception ex) {
            // Filet de securite : si un futur champ redevient non serialisable en JSON,
            // on affiche un message clair au lieu de laisser la page planter en plein rendu.
            model.addAttribute("sites", Collections.emptyList());
            model.addAttribute("overlapsJson", Collections.emptyList());
            model.addAttribute("mapError", "Impossible de preparer les donnees de la carte : " + ex.getMessage());
        }
        return "map";
    }

    @GetMapping("/export/kml")
    public ResponseEntity<byte[]> exportKml() {
        String kml = KmlExporter.export(repository.findAll());
        byte[] bytes = kml.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.google-earth.kml+xml"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sites_tunisie_telecom.kml\"")
                .body(bytes);
    }

    /**
     * Exporte un unique site au format KML, pour le bouton "Localiser sur
     * Google Earth" de la fiche technique. Ouvrir ce fichier lance
     * automatiquement Google Earth Pro (desktop) et centre la vue sur les
     * coordonnees GPS exactes du site (le fichier ne contient qu'un seul
     * Placemark).
     */
    @GetMapping("/sites/{id}/export/kml")
    public ResponseEntity<byte[]> exportSiteKml(@PathVariable int id) {
        Site site = repository.findById(id).orElse(null);
        if (site == null) {
            return ResponseEntity.notFound().build();
        }
        String kml = KmlExporter.export(Collections.singletonList(site));
        byte[] bytes = kml.getBytes(StandardCharsets.UTF_8);
        String filename = "site_" + id + "_google_earth.kml";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.google-earth.kml+xml"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }
}
