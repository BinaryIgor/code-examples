1. **Inserts**  
    * **MySQL** - `4383 QPS` with `42.729 ms` at the 99th percentile for single-row inserts;<br> for batch inserts of *100 rows* - `1883 QPS` with `146.497 ms` at the 99th percentile
    * **PostgreSQL** - `21 338 QPS` with `4.009 ms` at the 99th percentile for single-row inserts;<br> for batch inserts of *100 rows* - `3535 QPS` with `34.779 ms` at the 99th percentile
    * **MariaDB** - `18 241 QPS` with `7.337 ms` at the 99th percentile for single-row inserts;<br> for batch inserts of *100 rows* - `1941 QPS` with `136.598 ms` at the 99th percentile
2. **Selects**
    * **MySQL** - `33 469 QPS` with `12.721 ms` at the 99th percentile for single-row selects by id;<br> for sorted selects of multiple rows - `4559 QPS `with `41.294 ms` at the 99th percentile
    * **PostgresSQL** - `55 200 QPS` with `5.446 ms` at the 99th percentile for single-row selects by id;<br> for sorted selects of multiple rows - `4745 QPS` with `9.146 ms` at the 99th percentile
    * **MariaDB** - `54 095 QPS` with `3.811 ms` at the 99th percentile for single-row selects by id;<br> for sorted selects of multiple rows - `6668 QPS` with `55.712 ms` at the 99th percentile
3. **Updates**
    * **MySQL** - `3747 QPS` with `39.774 ms` at the 99th percentile for updates by id of multiple columns
    * **PostgreSQL** - `18 046 QPS` with `4.704 ms` at the 99th percentile for updates by id of multiple columns
    * **MariaDB** - `23 380 QPS` with `3.992 ms` at the 99th percentile for updates by id of multiple columns
4. **Deletes**
    * **MySQL** - `5596 QPS` with `43.039 ms` at the 99th percentile for deletes by id
    * **PostgreSQL** - `18 285 QPS` with `4.661 ms` at the 99th percentile for deletes by id
    * **MariaDB** - `25 000 QPS` with `5.843 ms` at the 99th percentile for deletes by id
5. **Inserts, Updates, Deletes and Selects mixed in 1:1 writes:reads proportion**
    * **MySQL** - `6300 QPS` with `40.635 ms` at the 99th percentile
    * **PostgreSQL** - `23 441 QPS` with `4.634 ms` at the 99th percentile
    * **MariaDB** - `28 347 QPS` with `5.317 ms` at the 99th percentile