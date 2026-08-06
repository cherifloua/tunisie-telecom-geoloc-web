# Guide de demarrage rapide

## Ce que tu veux : PC et telephone independants, chacun avec son icone

C'est possible et deja prevu dans le code (manifest + icones + service worker
sont deja en place). Il ne manque qu'**une seule chose** : heberger
l'application quelque part qui reste allume 24h/24 (donc pas ton PC).
Une fois ca fait, PC et telephone installent chacun leur icone directement
depuis leur navigateur et n'ont plus besoin l'un de l'autre.

Ca se fait en 2 etapes, une seule fois.

### Etape 1 - Heberger l'application en ligne (10 minutes, gratuit)

1. Cree un compte sur https://render.com (tu peux te connecter avec GitHub)
2. Mets ce dossier dans un depot GitHub (public ou prive)
3. Sur Render : **New +** -> **Web Service** -> connecte ton depot
4. Render detecte automatiquement le `Dockerfile` fourni (Docker = coche par
   defaut) -- aucune configuration supplementaire n'est necessaire
5. Choisis le plan **Free**, clique **Create Web Service**
6. Au bout de quelques minutes, Render donne une URL publique, du type :
   `https://tunisie-telecom-geoloc.onrender.com`

Cette URL est fixe : c'est elle qui remplace `http://localhost:8080` partout
a partir de maintenant. Garde-la de cote.

**A savoir sur le plan gratuit Render** :
- Le serveur se met en veille apres 15 min sans visite et prend ~30s a se
  reveiller au prochain acces -- normal, pas un bug.
- `data/sites.csv` (les sites enregistres) est reinitialise a chaque
  redeploiement, car le disque du plan gratuit n'est pas persistant. Si tu
  veux garder tes donnees durablement, ajoute un disque persistant Render
  (payant) ou une base de donnees plus tard -- pas necessaire pour demarrer.

*(Alternative a Render : Railway, Fly.io -- meme principe, le `Dockerfile`
fourni fonctionne tel quel sur les deux.)*

### Etape 2 - Installer l'icone sur le PC

1. Ouvre l'URL Render (ex: `https://tunisie-telecom-geoloc.onrender.com`)
   dans **Chrome** ou **Edge**.
2. Une icone d'installation apparait a droite de la barre d'adresse (ecran
   avec une fleche vers le bas, ou directement "Installer Tunisie Telecom").
   Clique dessus.
3. L'application s'installe comme un vrai programme : fenetre a part, sans
   onglets ni barre d'adresse, icone dans le menu Demarrer et sur le Bureau.

Le PC n'a **plus besoin de Java, de Maven, ni de faire tourner de serveur** :
il se connecte juste a l'URL en ligne, comme n'importe quelle app installee.

### Etape 3 - Installer l'icone sur le telephone

1. Ouvre la meme URL Render dans le navigateur du telephone (4G ou Wi-Fi,
   n'importe lequel -- plus besoin d'etre sur le meme reseau que le PC).
2. **Android (Chrome)** : menu ⋮ -> "Ajouter a l'ecran d'accueil" / "Installer
   l'application".
   **iPhone (Safari)** : bouton Partager -> "Sur l'ecran d'accueil".
3. Une icone Tunisie Telecom apparait sur l'ecran d'accueil, qui ouvre l'app
   en plein ecran, comme une vraie app.

**Resultat** : PC et telephone ont chacun leur icone, chacun se connecte
directement a l'URL en ligne, aucun des deux ne depend de l'autre ni n'a
besoin d'etre allume pour que l'autre fonctionne.

---

## Alternative : usage local sur le meme Wi-Fi (sans hebergement en ligne)

Utile pour tester rapidement en developpement, mais dans ce mode **le PC doit
rester allume** avec le serveur en cours d'execution pour que le telephone
puisse s'y connecter (donc pas independant -- pour ca, utilise l'Etape 1
ci-dessus).

Il te faut, une seule fois, installer sur le PC qui va faire tourner le
serveur :
- **Java 17 ou plus recent** : https://adoptium.net/fr/temurin/releases/?version=17

Maven n'a pas besoin d'etre installe separement : le projet inclut un "Maven
Wrapper" (`mvnw.cmd` / `mvnw`) qui le telecharge automatiquement au premier
lancement.

### Lancer le serveur local

- **Windows** : double-clique sur `LANCER-WINDOWS.bat`
- **Mac / Linux** : ouvre un terminal dans ce dossier et tape :
  ```bash
  ./lancer-mac-linux.sh
  ```

Le script affiche l'adresse IP du PC sur le reseau et ouvre
`http://localhost:8080` automatiquement.

### Raccourci Bureau (mode local)

Double-clique une seule fois sur `CREER-ICONE-BUREAU.bat` : une icone
"Tunisie Telecom" apparait sur le Bureau, qui lance `LANCER-WINDOWS.bat` (donc
demarre le serveur local a chaque clic). C'est different de l'icone "installee"
de l'Etape 2 : celle-ci a besoin que le PC fasse tourner Java a chaque fois.

### Depuis le telephone (meme Wi-Fi que le PC)

1. Verifie que le telephone est sur le **meme Wi-Fi** que le PC.
2. Ouvre `http://ADRESSE-IP-DU-PC:8080` (adresse affichee au lancement du
   script).
3. Installable a l'ecran d'accueil comme decrit a l'Etape 3, mais **le PC doit
   rester allume et le script doit continuer a tourner** pour que ca marche.

---

## En cas de probleme

- "java n'est pas reconnu" (mode local) -> Java n'est pas installe, ou il
  faut redemarrer l'invite de commande / le terminal apres l'installation.
- Pas d'icone "Installer" dans Chrome/Edge -> verifie que tu es bien sur
  l'URL `https://...` de Render (pas `http://localhost` en mode local sans
  certificat), attends quelques secondes que la page finisse de charger, ou
  recharge la page.
- Le telephone n'arrive pas a se connecter en mode local -> verifie que PC et
  telephone sont bien sur le meme Wi-Fi, et que le pare-feu Windows/Mac
  autorise Java/le port 8080.
- Render met ~30 secondes a repondre au premier acces apres une periode
  d'inactivite -> normal sur le plan gratuit, pas un bug.
