// Service worker minimal : necessaire pour que le navigateur propose
// "Ajouter a l'ecran d'accueil / Installer l'application" sur mobile.
// Ne met rien en cache (l'app a besoin du serveur pour fonctionner),
// il sert juste a rendre l'app "installable".

self.addEventListener('install', (event) => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  // Passe simplement toutes les requetes au reseau (pas de cache offline).
  event.respondWith(fetch(event.request));
});
