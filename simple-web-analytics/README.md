# Simple yet Scalable Web Analytics with JSON in SQL 

Simple Web Analytics that can easily scale to thousands of requests per seconds with rather limited resource requirements.

Requirements:
* Docker to run apps
* Java 21 + compatible Maven version; to build an example implementation, it can be rewritten to any backend programming language
* Bash for scripts

Start db (Postgres), from the `db` folder:
```
bash build_and_run.bash
```

Start backend:
```
bash build_and_run.bash
```

To see and generate some frontend events, just go to http://localhost:8080 and play with the page.

If you want to have lots of random data to test solution performance and query data, run:
```
bash generate_random_analytics_events.bash
```
This will start random analytics events generator, which you can see:
```
docker stats --no-stream
CONTAINER ID   NAME                         CPU %     MEM USAGE / LIMIT     MEM %     NET I/O   BLOCK I/O        PIDS
7d012b972f3d   analytics-events-generator   343.52%   366.9MiB / 1000MiB    36.69%    0B / 0B   0B / 45.1kB      514
11f0bfac8df9   analytics-backend            170.81%   584.3MiB / 1.953GiB   29.21%    0B / 0B   49.2kB / 479kB   49
80c69b09c459   analytics-db                 18.93%    71.35MiB / 1000MiB    7.14%     0B / 0B   0B / 191MB       9
```

\
Have fun experimenting!
