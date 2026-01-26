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

-- Extension pg_trgm pour recherche textuelle avec LIKE/ILIKE
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Index GIN avec opérateurs trigram pour recherches partielles (LIKE '%...%')
-- Impact attendu : 10-100x plus rapide sur les colonnes textuelles
CREATE INDEX idx_incident_title_trgm ON incident USING GIN (title gin_trgm_ops);
CREATE INDEX idx_incident_description_trgm ON incident USING GIN (description gin_trgm_ops);

-- Index B-tree pour filtre exact sur severity (LOW/MEDIUM/HIGH)
CREATE INDEX idx_incident_severity ON incident(severity);

-- Index GIN pour recherche owner (nom, prénom, email)
CREATE INDEX idx_person_last_name_trgm ON person USING GIN (last_name gin_trgm_ops);
CREATE INDEX idx_person_first_name_trgm ON person USING GIN (first_name gin_trgm_ops);
CREATE INDEX idx_person_email_trgm ON person USING GIN (email gin_trgm_ops);
