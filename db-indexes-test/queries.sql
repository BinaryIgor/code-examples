SELECT * FROM account where id = 'c96b44de-0472-48cd-b4d9-7ea383a52adb';

select * from account where attributes @> '{"name": "ada"}';
select * from account where attributes @> '{ "name": "ae1b1", "countryCode": 10 }';
select * from account where attributes @> '{"name": "ada"}';

select * from account where attributes @> '{ "name": "ae1b1" }' or attributes @> '{"name": "ae3"}';

CREATE INDEX account_name ON account(name);

SELECT pg_size_pretty(pg_relation_size('account'));
SELECT pg_size_pretty(pg_total_relation_size('account'));
SELECT pg_size_pretty(pg_relation_size('account_name'));
SELECT pg_size_pretty(pg_relation_size('account_attributes'));

