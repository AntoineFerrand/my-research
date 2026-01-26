-- Script de test de performance pour mesurer l'impact des optimisations
-- Exécuter avec : psql -U user -d incidents -f 03-performance-test.sql

-- Test 1 : Recherche par titre avec LIKE '%...%'
EXPLAIN ANALYZE
SELECT i.*, p.last_name, p.first_name, p.email
FROM incident i
JOIN person p ON i.owner_id = p.id
WHERE i.title LIKE '%Incident%'
ORDER BY i.created_at DESC
LIMIT 10;

-- Test 2 : Recherche par description
EXPLAIN ANALYZE
SELECT i.*, p.last_name, p.first_name, p.email
FROM incident i
JOIN person p ON i.owner_id = p.id
WHERE i.description LIKE '%Description%'
ORDER BY i.created_at DESC
LIMIT 10;

-- Test 3 : Recherche par severity
EXPLAIN ANALYZE
SELECT i.*, p.last_name, p.first_name, p.email
FROM incident i
JOIN person p ON i.owner_id = p.id
WHERE i.severity = 'HIGH'
ORDER BY i.created_at DESC
LIMIT 10;

-- Test 4 : Recherche par owner (nom)
EXPLAIN ANALYZE
SELECT i.*, p.last_name, p.first_name, p.email
FROM incident i
JOIN person p ON i.owner_id = p.id
WHERE p.last_name LIKE '%Smith%'
ORDER BY i.created_at DESC
LIMIT 10;

-- Test 5 : Recherche combinée (multi-critères)
EXPLAIN ANALYZE
SELECT i.*, p.last_name, p.first_name, p.email
FROM incident i
JOIN person p ON i.owner_id = p.id
WHERE i.title LIKE '%Incident%'
  AND i.severity = 'HIGH'
  AND p.last_name LIKE '%Smith%'
ORDER BY i.created_at DESC
LIMIT 10;

-- Afficher les statistiques des index
SELECT
    schemaname,
    relname as tablename,
    indexrelname as indexname,
    idx_scan as index_scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY relname, indexrelname;
