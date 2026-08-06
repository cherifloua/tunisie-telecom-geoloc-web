package com.telecomapp.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application web de geolocalisation et de gestion des sites de transmission
 * telecom (Tunisie Telecom), avec export Google Earth (KML).
 *
 * Contrairement a la version desktop (JavaFX), cette version tourne comme un
 * serveur web : elle est donc accessible depuis n'importe quel appareil
 * (PC, telephone, tablette) via un navigateur, du moment que l'appareil peut
 * atteindre l'adresse du serveur (reseau local, ou internet si deploye en ligne).
 */
@SpringBootApplication
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
