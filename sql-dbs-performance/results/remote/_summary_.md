1. **Inserts**  
   1. **MySQL** - `11 057 QPS` with `103.108 ms` at the 99th percentile for single-row inserts;
    <br>`1265 QPS` with `214.238 ms` at the 99th percentile for batch inserts of *100 rows*
   2. **PostgreSQL** - `18 337 QPS` with `5.542 ms` at the 99th percentile for single-row inserts;
    <br>`1811 QPS` with `85.886 ms` at the 99th percentile for batch inserts of *100 rows*
   3. **MariaDB** - `18 750 QPS` with `4.543 ms` at the 99th percentile for single-row inserts;
    <br>`1219 QPS` with `255.328 ms` at the 99th percentile for batch inserts of *100 rows*
2. **Selects**
   1. **MySQL** - `22 782 QPS` with `5.347 ms` at the 99th percentile for single-row selects by id;
    <br>`2978 QPS `with `82.982 ms` at the 99th percentile for sorted selects of multiple rows;
    <br>`17 214 QPS` with `8.721 ms` at the 99th percentile for selects by id with two joins
   2. **PostgresSQL** - `34 674 QPS` with `3.322 ms` at the 99th percentile for single-row selects by id;
    <br>`3082 QPS` with `47.423 ms` at the 99th percentile for sorted selects of multiple rows;
    <br>`17 167 QPS` with `6.372 ms` at the 99th percentile for selects by id with two joins
   3. **MariaDB** - `36 472 QPS` with `4.196 ms` at the 99th percentile for single-row selects by id;
    <br>`4552 QPS` with `51.217 ms` at the 99th percentile for sorted selects of multiple rows;
    <br>`24 616 QPS` with `7.337 ms` at the 99th percentile for selects by id with two joins
3. **Updates**
   1. **MySQL** - `7795 QPS` with `103.772 ms` at the 99th percentile for updates by id of multiple columns
   2. **PostgreSQL** - `18 258 QPS` with `4.69 ms` at the 99th percentile for updates by id of multiple columns
   3. **MariaDB** - `19 990 QPS` with `4.601 ms` at the 99th percentile for updates by id of multiple columns
4. **Deletes**
   1. **MySQL** - `8136 QPS` with `105.97 ms` at the 99th percentile for deletes by id
   2. **PostgreSQL** - `19 712 QPS` with `4.714 ms` at the 99th percentile for deletes by id
   3. **MariaDB** - `21 386 QPS` with `19.152 ms` at the 99th percentile for deletes by id
5. **Inserts, Updates, Deletes and Selects mixed in 1:1 writes:reads proportion**
   1. **MySQL** - `12 375 QPS` with `95.753 ms` at the 99th percentile
   2. **PostgreSQL** - `21 858 QPS` with `7.758 ms` at the 99th percentile
   3. **MariaDB** - `23 875 QPS` with `14.124 ms` at the 99th percentile