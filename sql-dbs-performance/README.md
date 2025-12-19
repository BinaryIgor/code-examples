# SQL DBs Performance Tests

Set of scripts and configs to make a detailed SQL DBs performance tests and comparisons.

Cmds:
```
select pg_size_pretty(pg_total_relation_size('user'));

SELECT TABLE_SCHEMA AS DB_Name, SUM(DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024 AS DB_Size FROM information_schema.TABLES WHERE TABLE_SCHEMA="mysql";
SELECT TABLE_NAME AS "Tab_Name", ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS "Tab_Size" FROM information_schema.TABLES WHERE table_schema = "mysql" order by Tab_Size desc;


show shared_buffers;

show variables like 'inno%';

SELECT @@transaction_ISOLATION;

SET FOREIGN_KEY_CHECKS = 1;
```

TODO
* different optimal pool size for read- vs write-heavy workloads?

