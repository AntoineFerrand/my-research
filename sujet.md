# Sujet de mise en situation - Développeur Fullstack expérimenté

## Objectif

Développer une application web simple en Spring Boot + Angular, connectée à une
base PostgreSQL via Docker Compose, permettant de rechercher des incidents dans
une base contenant 100 000 entrées.
L'objectif est de mesurer et améliorer les performances de cette recherche.

## Étapes à réaliser

### 1. Dépôt Git public

Créer un dépôt Git (GitHub, GitLab, etc.) **accessible publiquement**.
Tous les fichiers et instructions doivent être présents dans ce dépôt.
Chaque amélioration doit faire l'objet d'un commit distinct et explicite.

### 2. Initialisation de l'environnement

Créer un script SQL **01-ddl.sql**.
Il initialise la base avec deux tables _person_ et _incident_.

```sql
CREATE TABLE person (
  id SERIAL PRIMARY KEY,
  last_name TEXT NOT NULL,
  first_name TEXT NOT NULL,
  email TEXT NULL
);

CREATE TABLE incident (
  id SERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  description TEXT NOT NULL,
  severity TEXT NOT NULL,
  owner_id INTEGER NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  FOREIGN KEY (owner_id) REFERENCES person(id)
);

CREATE INDEX idx_person_id ON person(id);
CREATE INDEX idx_incident_id ON incident(id);
```

Créer un script SQL **02-data.sql**.
Il insère quelques personnes et 100 000 lignes aléatoires dans la base.

```sql
INSERT INTO person (last_name, first_name, email) VALUES
  ('Smith', 'John', 'john.smith@company.com'),
  ('Johnson', 'Sarah', 'sarah.johnson@company.com'),
  ('Brown', 'Michael', 'michael.brown@company.com'),
  ('Davis', 'Emily', 'emily.davis@company.com'),
  ('Wilson', 'David', 'david.wilson@company.com'),
  ('Miller', 'Jessica', 'jessica.miller@company.com'),
  ('Garcia', 'Robert', 'robert.garcia@company.com'),
  ('Martinez', 'Lisa', 'lisa.martinez@company.com'),
  ('Anderson', 'James', 'james.anderson@company.com'),
  ('Taylor', 'Amanda', 'amanda.taylor@company.com'),
  ('Thomas', 'Christopher', 'christopher.thomas@company.com'),
  ('Jackson', 'Michelle', 'michelle.jackson@company.com'),
  ('White', 'Daniel', 'daniel.white@company.com'),
  ('Harris', 'Jennifer', 'jennifer.harris@company.com'),
  ('Martin', 'Kevin', 'kevin.martin@company.com');

INSERT INTO incident (title, description, severity, owner_id, created_at)
SELECT
  'Incident ' || md5(random()::text),
  'Description: ' || md5(random()::text),
  CASE WHEN random() < 0.33 THEN 'LOW' WHEN random() < 0.66 THEN 'MEDIUM' ELSE 'HIGH' END,
  FLOOR(RANDOM() * ((SELECT MAX(id) FROM person) - (SELECT MIN(id) FROM person) + 1)) + (SELECT MIN(id) FROM person),
  NOW() - (random() * interval '365 days')
FROM generate_series(1, 100000);
```

Créer le fichier **compose.yaml**.
Il déclare la base de données dont le port est mappé sur l'hôte local.

```yaml
volumes:
  pgdata:

services:
  database:
    image: postgres:17
    environment:
      POSTGRES_DB: incidents
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./01-ddl.sql:/docker-entrypoint-initdb.d/01-ddl.sql
      - ./02-data.sql:/docker-entrypoint-initdb.d/02-data.sql
```

**Option 1** : Améliorer le fichier pour éviter les variables et/ou mots de
passe directement dedans.

**Option 2** : Créer un fichier **.gitattributes** pour rendre portable le
dépôt entre Windows et Linux (fin de ligne des fichiers textes extraits).

### 3. Backend

Dans un répertoire **backend**, créer une application **Spring Boot**.

Créer un fichier **.gitignore** et le remplir en conséquence.

Utiliser **Hibernate** pour interagir avec la base.

L'application sera une API REST simple.

Créer un endpoint **GET /incidents** avec les filtres suivants (tous
optionnels) : _title_, _description_, _severity_, _owner_.

Ce sont des filtres sur les chaînes (`colonne LIKE '%motif%'`).
Pour le owner, on testera le nom, prénom et l'e-mail : une seule correspondance
au motif avec l'un de ces champs suffira.

### 4. Frontend

Dans un répertoire **frontend**, créer une application **Angular**.

Mettre à jour le fichier **.gitignore**.

Ce sera une page unique avec :

- Un label et un champ pour chaque filtre ;
- Un bouton _Rechercher_ ;
- Un label affichant le temps de la dernière requête exécutée, calculé côté
  frontend (ex. `0.123 seconds`).
- Un tableau affichant les résultats avec comme colonnes :
  - Les attributs d'un incident ;
  - Les attributs de son _owner_ : nom, prénom et e-mail.

**Option 1** : Internationaliser le code.

**Option 2** : Faire un visuel agréable.

### 5. Optimiser l'application

À partir de la version initiale non optimisée, proposer des améliorations
techniques.
Voici des exemples :

- Ajout d'index (ex. _GIN_ avec _pg_trgm_ sur les colonnes textuelles).
- Mise en place d'une pagination.
- Utilisation d'un _eager loading_ côté backend.
- Ajout d'un cache (ex. côté backend et/ou navigateur).
- Autres optimisations pertinentes.

Chaque amélioration doit être :

- Documentée dans le _README.md_ :
  - Type d'optimisation ;
  - Intérêt ;
  - Moyenne de temps d'exécution avec le gain ou la régression constatée ;
- Committée séparément avec un message clair (code + README.md).

Livrables attendus :

- Dépôt Git public (envoyer l'URL) avec :
- _compose.yaml_, _01-ddl.sql_, _02-data.sql_ ;
- Code source Spring Boot dans _backend_ ;
- Code source Angular dans _frontend_ ;
- _README.md_ avec :
  - Instructions pour lancer et nettoyer l'environnement ;
  - Commandes pour générer les données ;
  - Explication des optimisations apportées.

Critères d'évaluation :

- Fonctionnalité correcte de l'application ;
- Qualité du code (structure, clarté, tests éventuels) ;
- Pertinence des optimisations ;
- Utilisation rigoureuse de Git ;
- Documentation claire et complète.
