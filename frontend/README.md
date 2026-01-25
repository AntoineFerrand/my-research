# Frontend - Application de Recherche d'Incidents

Application Angular 21 pour rechercher et filtrer des incidents.

## 🚀 Démarrage rapide

### Lancer l'environnement complet (backend + frontend + base de données)

Depuis la racine du projet :

```bash
# Démarrer tous les services avec Docker Compose
docker compose up -d

# Accéder à l'application
# Frontend : http://localhost:4200
# Backend : http://localhost:8080
```

### Lancer uniquement le frontend (développement)

```bash
cd frontend
npm install
npm start
```

L'application sera accessible sur `http://localhost:4200/`.

## 🗄️ Génération des données de test

Les données sont générées automatiquement au démarrage de la base de données via les scripts SQL :

- `scripts-sql/01-ddl.sql` : Création des tables
- `scripts-sql/02-data.sql` : Insertion des données de test (100 personnes, 1000 incidents)

Pour régénérer les données :

```bash
# Arrêter et supprimer les conteneurs
docker compose down -v

# Redémarrer (les scripts SQL seront rejoués)
docker compose up -d
```

## 🧹 Nettoyage

```bash
# Arrêter les services
docker compose down

# Arrêter et supprimer les volumes (supprime les données)
docker compose down -v

# Nettoyer le cache npm (frontend)
cd frontend
rm -rf node_modules package-lock.json
npm install
```
