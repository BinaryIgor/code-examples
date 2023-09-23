import pg from 'pg';
import { randomUUID } from 'crypto';

const TO_INSERT = 15_000_000;
const BATCH_SIZE = 1000;
const BATCHES_TO_INSERT = TO_INSERT / BATCH_SIZE;
const BATCHES_TO_WAIT = 100;

const DISTINCT_NAMES = 1_000_000;
const COUNTRY_CODES = 100;

const pool = new pg.Pool({
    host: 'localhost',
    port: 5555,
    user: 'postgres',
    password: 'postgres',
    max: 20
});

function randomAccounts(size) {
    const acconts = [];
    for (let i = 0; i < size; i++) {
        const name = randomString(DISTINCT_NAMES);
        const countryCode = randomNumber(0, COUNTRY_CODES);
        acconts.push({
            id: randomUUID(),
            name: name,
            countryCode: countryCode,
            attributes: JSON.stringify({
                name: name,
                countryCode: countryCode
            })
        });
    }
    return acconts;
}

function randomString(distinctiveness) {
    const value = Math.round(Math.random() * distinctiveness);
    return value.toString(16);
}

function randomNumber(min, max) {
    return Math.round(min + (Math.random() * (max - min)));
}

try {
    const start = Date.now();

    let doneBatches = 0;
    let inserts = [];

    console.log(`Inserting data in ${BATCHES_TO_INSERT} batches of ${BATCH_SIZE} each...`);

    for (let i = 0; i < BATCHES_TO_INSERT; i++) {
        const accounts = randomAccounts(BATCH_SIZE);

        let insert = `INSERT INTO account(id, name, country_code, attributes) 
            VALUES($1::uuid, $2, $3, $4::jsonb), `;
        let insertArgs = [];
        let inserParams = [];

        let lastParam = 4;

        accounts.forEach((a, idx) => {
            if (idx > 0) {
                insertArgs.push(`($${lastParam + 1}::uuid, $${lastParam + 2}, $${lastParam + 3}, $${lastParam + 4}::jsonb)`);
                lastParam += 4;
            }
            inserParams.push(a.id);
            inserParams.push(a.name);
            inserParams.push(a.countryCode);
            inserParams.push(a.attributes);
        });

        insert = insert + '\n' + insertArgs.join(",\n");

        inserts.push(pool.query(insert, inserParams));

        if (inserts.length >= BATCHES_TO_WAIT) {
            console.log(`Waiting for ${inserts.length} insert batches of ${doneBatches * BATCHES_TO_WAIT}/${BATCHES_TO_INSERT} batches...`);
            await Promise.all(inserts);
            inserts = [];
            doneBatches++;
        }
    }

    if (inserts.length > 0) {
        console.log(`Waiting for last ${inserts.length} batch inserts...`);
        await Promise.all(inserts);
    }

    const duration = (Date.now() - start) / 1000;

    console.log(`Preparing data took ${duration} seconds`);

    pool.end();
} catch (e) {
    console.log("Experienced some problems when inserting...", e);
}