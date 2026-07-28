#!/bin/bash
set -e

echo "============================================================"
echo "  Tunisie Telecom - Geolocalisation des sites"
echo "  Demarrage de l'application..."
echo "============================================================"
echo

if ! command -v java &> /dev/null; then
    echo "[ERREUR] Java n'est pas installe sur cette machine."
    echo "Installez Java 17 ou plus recent : https://adoptium.net/fr/temurin/releases/?version=17"
    exit 1
fi

echo "Adresse(s) IP de cette machine sur le reseau (pour te connecter depuis ton telephone) :"
echo "------------------------------------------------------------"
if command -v ipconfig &> /dev/null; then
    ipconfig getifaddr en0 2>/dev/null || true
    ipconfig getifaddr en1 2>/dev/null || true
else
    hostname -I 2>/dev/null || ip -4 addr show | grep inet
fi
echo "------------------------------------------------------------"
echo
echo "Sur ton telephone (connecte au MEME Wi-Fi que cette machine), ouvre :"
echo "   http://ADRESSE-IP-CI-DESSUS:8080"
echo
echo "Sur cette machine, ouvre :"
echo "   http://localhost:8080"
echo
echo "Premier lancement : le script va telecharger Maven puis les"
echo "dependances du projet (besoin d'internet la 1ere fois seulement),"
echo "ca peut prendre quelques minutes."
echo
echo "Pour arreter l'application : Ctrl+C"
echo "============================================================"
echo

( sleep 3 && (open http://localhost:8080 2>/dev/null || xdg-open http://localhost:8080 2>/dev/null || true) ) &

chmod +x ./mvnw
export PORT=8080
./mvnw spring-boot:run
