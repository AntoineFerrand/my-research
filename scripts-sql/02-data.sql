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
