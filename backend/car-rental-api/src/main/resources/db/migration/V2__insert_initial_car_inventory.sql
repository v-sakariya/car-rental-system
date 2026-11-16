
-- Five sedans
INSERT INTO cars (id, type, active)
VALUES
    (gen_random_uuid(), 'SEDAN', TRUE),
    (gen_random_uuid(), 'SEDAN', TRUE),
    (gen_random_uuid(), 'SEDAN', TRUE),
    (gen_random_uuid(), 'SEDAN', TRUE),
    (gen_random_uuid(), 'SEDAN', TRUE);

-- Three SUVs
INSERT INTO cars (id, type, active)
VALUES
    (gen_random_uuid(), 'SUV', TRUE),
    (gen_random_uuid(), 'SUV', TRUE),
    (gen_random_uuid(), 'SUV', TRUE);

-- Two vans
INSERT INTO cars (id, type, active)
VALUES
    (gen_random_uuid(), 'VAN', TRUE),
    (gen_random_uuid(), 'VAN', TRUE);
