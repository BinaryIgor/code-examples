27.01 - start

## Links

- https://www.jimmybogard.com/document-level-pessimistic-concurrency-in-mongodb-now-with-intent-locks/
- https://hub.docker.com/_/mongo
- https://aerospike.com/blog/document-databases-features-benefits-challenges
- https://stackoverflow.com/questions/18488209/does-mongodb-journaling-guarantee-durability
- https://www.mongodb.com/docs/manual/core/clustered-collections/#std-label-clustered-collections
- https://www.mongodb.com/docs/manual/data-modeling/

## Queries/commands/configs

```
db.workers.explain("executionStats").find({_id:''});
db.accounts.stats();
db.accounts.stats().size / db.accounts.stats().storageSize;

db.serverStatus().wiredTiger.cache["maximum bytes configured"]

db.serverStatus().connections

select (pg_table_size('products') / (select count(*) from products));

 select jsonbb_array_elements(data->'categories') as category, count(*) from products group by category limit 10;
 select jsonb_array_elements(data->'tags') as tags, 
    count(*) as products,
    min(data->>'createdAt') as oldest_product_created_product_at,
    max(data->>'createdAt') as latest_product_created_at,
    min(jsonb_array_length(data->'variations')) as min_variations,
    max(jsonb_array_length(data->'variations')) as max_variations
    from products where data->'categories' @> '["Desktop Computers"]'::jsonb group by tags;
    
db.products.aggregate([
  { $match: { _id: { $in: [
   UUID('910308cf-679f-48f2-8a32-3c0b031ac909'),
   UUID('6b9bd519-09dd-49f1-9af3-1d51b97c09d0'),
   UUID('ce179826-af65-4612-87a2-319295f73f35'),
   UUID('211ffab6-a02d-4d1a-b07b-a6af7e27bedf'),
   UUID('980c0011-4787-468b-a671-4b9f71ccf2b2'),
   UUID('4dc0d599-dfdd-4afa-b784-791b7def2fc6')
   ] } } },
  { $unwind: "$tags" },
  { $group: 
    { 
      _id: "$tags", 
      products: { $sum: 1 },
      oldestProductCreatedAt: { $min: "$createdAt" },
      latestProductCreatedAt: { $max: "$createdAt" },
      minVariations: { $min: { $size: "$variations" } },
      maxVariations: { $max: { $size: "$variations" } }
    }
  },
  { 
    $project: { 
      _id: 0, 
      tag: "$_id", 
      products: 1,
      oldestProductCreatedAt: 1,
      latestProductCreatedAt: 1,
      minVariations: 1,
      maxVariations: 1
    } 
  }
]);

db.products.aggregate([
  { $match: { _id: { $in: ['bbac46a5-a166-4e40-ad62-778e4ff24b90', '0196f455-06d5-4067-ac01-4e1dbf99c866'] } } }
]);

explain analyze select
  jsonb_array_elements(data->'tags') as tag, 
  count(*) as products,
  min(data->>'createdAt') as oldest_product_created_product_at,
  max(data->>'createdAt') as latest_product_created_at,
  min(jsonb_array_length(data->'variations')) as min_variations,
  max(jsonb_array_length(data->'variations')) as max_variations
 from products
 where data->>'id' in (select data->>'id' from products limit 10)
 group by tag;
```
