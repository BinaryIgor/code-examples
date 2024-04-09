CREATE TABLE IF NOT EXISTS account (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL
);

CREATE PROCEDURE generate_db_data(batches INTEGER)
LANGUAGE plpgsql
AS
$$
BEGIN
    raise notice 'About to generate data in % batches of 10 000 rows each...', batches;
    FOR i IN 1..batches LOOP
        raise notice 'Executing batch %/%...', i, batches;

        WITH random_uuids AS (
            SELECT generate_series(1, 10000) AS idx, gen_random_uuid() as id
        ),
        random_accounts AS (
            SELECT id, CONCAT('name-', id) AS name, CONCAT('email-', id, '@email.com') AS email
            FROM random_uuids
        )
        INSERT INTO account (id, name, email)
        SELECT id, name, email FROM random_accounts;

    END LOOP;
    raise notice 'All data batches executed!';
END;
$$;

