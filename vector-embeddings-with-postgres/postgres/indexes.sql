SET maintenance_work_mem=602400;

CREATE INDEX vector_embedding_open_ai_text_3_small_ifflat
    ON vector_embedding_open_ai_text_3_small USING ivfflat (embedding vector_l2_ops) WITH (lists=250);

CREATE INDEX vector_embedding_open_ai_text_3_small_hnsw
    ON vector_embedding_open_ai_text_3_small USING hnsw (embedding vector_l2_ops) WITH (m = 16, ef_construction = 64);

SELECT pg_size_pretty(pg_total_relation_size('vector_embedding_open_ai_text_3_small'));
SELECT pg_size_pretty(pg_relation_size('vector_embedding_open_ai_text_3_small'));
SELECT pg_size_pretty(pg_indexes_size('vector_embedding_open_ai_text_3_small'));
