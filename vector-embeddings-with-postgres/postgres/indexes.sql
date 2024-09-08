SET maintenance_work_mem=602400;

CREATE INDEX vembedding_performance_test_performance_test_ifflat
    ON vembedding_performance_test_performance_test USING ivfflat (embedding vector_l2_ops) WITH (lists=100);

SELECT pg_size_pretty(pg_total_relation_size('vector_embedding_open_ai_text_3_small'));
SELECT pg_size_pretty(pg_relation_size('vector_embedding_open_ai_text_3_small'));
SELECT pg_size_pretty(pg_indexes_size('vector_embedding_open_ai_text_3_small'));
