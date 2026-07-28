# Tunisie Telecom - Geolocalisation des sites (version Web)

> **Tu veux que le PC et le telephone aient chacun leur icone, independamment
> l'un de l'autre ?** Va directement dans `GUIDE-DEMARRAGE-RAPIDE.md`, la
> reponse y est expliquee pas a pas.

Version **web** de l'application de gestion et de geolocalisation des sites
telecom (2G/3G/4G/5G), avec export Google Earth (KML). Contrairement a la
version desktop (JavaFX), celle-ci tourne comme un **serveur** (Spring Boot) :
elle est donc accessible depuis **n'importe quel navigateur** — PC, telephone,
tablette — a condition que l'appareil puisse atteindre l'adresse du serveur.

## Fonctionnalites (identiques a la version desktop)

- CRUD complet des sites telecom (formulaire organise en sections repliables :
  General & Position, Infrastructure, Antenne, Radio, Energie, Exploitation)
- Recherche par nom, ville, region ou technologie
- Carte GPS reelle (OpenStreetMap / Leaflet)
- Export Google Earth (fichier `.kml` telechargeable, fiche technique complete
  par site)
- Meme modele de donnees que la version desktop (`Site.java` est repris a
  l'identique)

## 1. Lancer en local (sur ton PC)

```bash
cd tunisie-telecom-geoloc-web
mvn spring-boot:run
```

Puis ouvre `http://localhost:8080` dans ton navigateur.

## 2. Acceder depuis ton telephone (meme reseau Wi-Fi)

1. Trouve l'adresse IP locale de ton PC (Windows : `ipconfig`, cherche "Adresse IPv4",
   ex: `192.168.1.15`)
2. Sur ton telephone connecte au **meme Wi-Fi**, ouvre `http://192.168.1.15:8080`

## 3. Acceder depuis n'importe quel reseau (4G, autre Wi-Fi...) — deploiement en ligne

Pour que l'appli soit accessible **sans etre sur le meme reseau**, il faut
l'heberger sur un serveur accessible depuis internet. Voici la methode la plus
simple et gratuite avec **Render** :

### Etapes (Render.com, gratuit)

1. Cree un compte sur https://render.com (tu peux te connecter avec GitHub)
2. Mets ce dossier `tunisie-telecom-geoloc-web` dans un depot GitHub (public ou prive)
3. Sur Render : **New +** -> **Web Service**
4. Connecte ton depot GitHub
5. Render detecte automatiquement le `Dockerfile` fourni (ou choisis manuellement
   "Docker" comme environnement) — aucune configuration supplementaire n'est necessaire
6. Choisis le plan **Free**
7. Clique **Create Web Service** — au bout de quelques minutes, Render te donne une
   URL publique du type `https://tunisie-telecom-geoloc.onrender.com`
8. **Cette URL fonctionne depuis n'importe quel appareil, n'importe quel reseau** :
   PC, telephone, 4G, Wi-Fi different...

### A savoir sur le plan gratuit de Render

- Le serveur peut se "mettre en veille" apres 15 minutes sans visite, et prendre
  ~30 secondes a se reveiller au prochain acces — normal sur un plan gratuit,
  pas un bug
- **Le stockage des sites (`data/sites.csv`) est reinitialise a chaque redeploiement**
  car le disque de Render (plan gratuit) n'est pas persistant. Pour une vraie
  persistance en production, il faudrait ajouter une base de donnees (ex: le plan
  gratuit PostgreSQL de Render) — pas necessaire pour une demo de stage, mais bon
  a savoir si tu veux garder tes donnees a long terme

### Alternative rapide pour une demo ponctuelle : ngrok

Si tu veux juste montrer l'appli une fois sans deployer en ligne durablement :

```bash
mvn spring-boot:run
```

puis dans un autre terminal (apres avoir installe https://ngrok.com/) :

```bash
ngrok http 8080
```

Ngrok te donne une URL publique temporaire (ex: `https://abcd1234.ngrok.io`)
qui redirige vers ton PC — fonctionne depuis n'importe quel reseau, mais
seulement pendant que ton PC et la commande `ngrok` tournent.

## Structure du projet

```
tunisie-telecom-geoloc-web/
├── pom.xml
├── Dockerfile
├── src/main/java/com/telecomapp/web/
│   ├── WebApplication.java        <- point d'entree Spring Boot
│   ├── model/
│   │   ├── Site.java              <- identique a la version desktop
│   │   └── SiteRepository.java    <- meme stockage CSV local, +thread-safe
│   ├── util/
│   │   └── KmlExporter.java       <- genere le KML pour telechargement HTTP
│   └── web/
│       ├── SiteFormData.java      <- DTO de formulaire (liaison Thymeleaf)
│       └── SiteController.java    <- toutes les routes (dashboard, CRUD, carte, export)
└── src/main/resources/
    ├── application.properties
    ├── static/
    │   ├── images/tt-logo.png
    │   └── css/style.css
    └── templates/
        ├── fragments/layout.html  <- menu lateral partage
        ├── dashboard.html
        ├── map.html
        └── sites/
            ├── list.html          <- liste + recherche
            ├── form.html          <- ajout/modification (sections repliables)
            └── detail.html        <- fiche technique en lecture seule
```
