# The Order of Data

Just a useful setup for running examples described in the related blog post: https://binaryigor.com/the-order-of-data.html

Some commands:
```
docker exec -it the-order-of-data-mongodb mongosh "mongodb://mongo:mongo@localhost:27017/test?authSource=admin"
docker exec -it the-order-of-data-mysql mysql --database mysql -p
docker exec -it the-order-of-data-postgresql psql -U postgres
```

Generate lots of random data (Postgres):
```
INSERT INTO account (id, name, created_at)
SELECT gen_random_uuid(), concat('acc', '-', n), now()
FROM generate_series(1, 10_000_000) AS n;
```