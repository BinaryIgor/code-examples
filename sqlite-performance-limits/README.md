# SQLite Performance Limits

Let's check what the simplest database can handle!

Requirements:
* Java 21 + compatible Maven version
* Docker
* Bash for scripts

All we have is a test tool written in Java + a few bash script to make the whole process easier.
SQLite is embedded, in-process, single-file (almost) database, so that's all we need.
We will run it in Docker, since it is trivial to limit process resources and observe its usage.

## Preparation

First, let's build the test tool:
```
bash build_tests.bash
```
it builds java jar and prepares associated Docker image.

Then, we want to run tests against dataset of a respectful size. Let's insert 1 million records into our test table:
```
bash run_batch_inserts_test_case.bash
```

--- 

* important to note: WAL mode!