Méthode utilisée:

J'ai tapé la commende ci dessous:
docker exec -it database psql -U user -d incidents

puis une fois connecté au conteneur, j'ai copier collé les tests un par un pour essayer.


Ensuite pour analyser le résultat, j'ai demandé à copilot de lancer le script en entier et d'analyser les résultats, et de conclure si les indexs utilisés ont été efficaces.

docker exec -i spring-boot-angular-test_database_1 psql -U user -d incidents -f /scripts-sql/03-performance-test.sql


## 📊 Analyse des Tests de Performance

### Résumé des Résultats

| Test                       | Temps d'exécution | Index utilisé ?       | Type de scan                |
|----------------------------|-------------------|-----------------------|-----------------------------|
| Test 1 - Title LIKE        | 30.8 ms           | ❌ Non                | Parallel Seq Scan           |
| Test 2 - Description LIKE  | 29.0 ms           | ❌ Non                | Parallel Seq Scan           |
| Test 3 - Severity = 'HIGH' | **13.6 ms**       | ✅ Oui                | Bitmap Index Scan           |
| Test 4 - Owner LIKE        | 15.1 ms           | ❌ Non                | Seq Scan                    |
| Test 5 - Multi-critères    | **10.7 ms**       | ✅ Partiel (severity) | Bitmap Index Scan + filtres |

### 🔍 Analyse Détaillée

**1. Index GIN Trigram sur `incident.title` - ⚠️ PEU EFFICACE**
- **Statut** : 0 scans utilisés
- **Raison** : PostgreSQL choisit un scan parallèle car trop de lignes correspondent (100% des incidents contiennent "Incident")
- **Conclusion** : Index non utilisé car la sélectivité est trop faible

**2. Index GIN Trigram sur `incident.description` - ⚠️ PEU EFFICACE**
- **Statut** : 0 scans utilisés
- **Raison** : Même problème, toutes les descriptions contiennent "Description"
- **Conclusion** : Index non utilisé, données de test trop uniformes

**3. Index B-tree sur `incident.severity` - ✅ TRÈS EFFICACE**
- **Statut** : **6 scans, 290k tuples lus**
- **Performance** : 13.6 ms vs 30 ms (gain de 2.2x)
- **Utilisation** : "Bitmap Index Scan on idx_incident_severity"
- **Conclusion** : **Index très pertinent**, utilisé systématiquement, améliore les performances

**4. Index GIN Trigram sur `person.last_name` - ❌ NON UTILISÉ**
- **Statut** : 0 scans utilisés
- **Raison** : Seulement 15 personnes dans la base, PostgreSQL préfère un Seq Scan
- **Conclusion** : Index inutile avec si peu de données

**5. Index sur `person.email` et `first_name` - ❌ NON UTILISÉS**
- **Statut** : 0 scans
- **Conclusion** : Jamais testés dans ces requêtes

### 💡 Recommandations

**À conserver :**
- ✅ `idx_incident_severity` - clairement bénéfique, utilisé activement

**À réévaluer :**
- ⚠️ Index GIN trigram - pourraient être utiles avec des données plus variées et des recherches plus sélectives
- ❌ Index sur `person` - inutiles avec seulement 15 personnes

**Points à noter :**
1. Les index trigram GIN ne sont pas utilisés car les données de test sont trop homogènes (tous les titres contiennent "Incident")
2. Pour tester réellement les index trigram, essayez des recherches plus spécifiques comme `LIKE '%urgent%'` ou `LIKE '%réseau%'`
3. PostgreSQL ne les utilisera que si la recherche est suffisamment sélective (< 10-20% des lignes)