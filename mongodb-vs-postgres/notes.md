27.01 - start

## Links

- https://www.jimmybogard.com/document-level-pessimistic-concurrency-in-mongodb-now-with-intent-locks/
- https://hub.docker.com/_/mongo
- https://aerospike.com/blog/document-databases-features-benefits-challenges
- https://stackoverflow.com/questions/18488209/does-mongodb-journaling-guarantee-durability
- https://www.mongodb.com/docs/manual/core/clustered-collections/#std-label-clustered-collections
- https://www.mongodb.com/docs/manual/data-modeling/

## Queries/commands

```
db.workers.explain("executionStats").find({_id:''});
```