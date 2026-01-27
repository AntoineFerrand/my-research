Method used:
docker exec -i spring-boot-angular-test_database_1 psql -U user -d incidents -f /scripts-sql/03-performance-test.sql

## Performance Test Analysis

### Results Summary

| Test                       | Execution Time    | Index Used?           | Scan Type                   |
|----------------------------|-------------------|-----------------------|-----------------------------|
| Test 1 - Title LIKE        | 30.8 ms           | ❌ No                 | Parallel Seq Scan           |
| Test 2 - Description LIKE  | 29.0 ms           | ❌ No                 | Parallel Seq Scan           |
| Test 3 - Severity = 'HIGH' | **13.6 ms**       | ✅ Yes                | Bitmap Index Scan           |
| Test 4 - Owner LIKE        | 15.1 ms           | ❌ No                 | Seq Scan                    |
| Test 5 - Multi-criteria    | **10.7 ms**       | ✅ Partial (severity) | Bitmap Index Scan + filters |

### Detailed Analysis

**1. GIN Trigram Index on `incident.title` - ⚠️ LOW EFFICIENCY**
- **Status**: 0 scans used
- **Reason**: PostgreSQL chooses a parallel scan because too many rows match (100% of incidents contain "Incident")
- **Conclusion**: Index not used due to low selectivity

**2. GIN Trigram Index on `incident.description` - ⚠️ LOW EFFICIENCY**
- **Status**: 0 scans used
- **Reason**: Same problem, all descriptions contain "Description"
- **Conclusion**: Index not used, test data too uniform

**3. B-tree Index on `incident.severity` - ✅ HIGHLY EFFECTIVE**
- **Status**: **6 scans, 290k tuples read**
- **Performance**: 13.6 ms vs 30 ms (2.2x improvement)
- **Usage**: "Bitmap Index Scan on idx_incident_severity"
- **Conclusion**: **Highly relevant index**, consistently used, improves performance

**4. GIN Trigram Index on `person.last_name` - ❌ NOT USED**
- **Status**: 0 scans used
- **Reason**: Only 15 people in the database, PostgreSQL prefers a Seq Scan
- **Conclusion**: Useless index with so little data

**5. Index on `person.email` and `first_name` - ❌ NOT USED**
- **Status**: 0 scans
- **Conclusion**: Never tested in these queries

### Recommendations

**To Keep:**
- ✅ `idx_incident_severity` - clearly beneficial, actively used

**To Reevaluate:**
- ⚠️ GIN trigram indexes - could be useful with more varied data and more selective searches
- ❌ Indexes on `person` - useless with only 15 people

**Key Points:**
1. GIN trigram indexes are not used because test data is too homogeneous (all titles contain "Incident")
2. To truly test trigram indexes, try more specific searches like `LIKE '%urgent%'` or `LIKE '%network%'`
3. PostgreSQL will only use them if the search is sufficiently selective (< 10-20% of rows)