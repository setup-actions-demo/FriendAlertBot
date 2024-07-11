
CREATE TABLE visitors_count_log (
    timestamp TIMESTAMP,
    campus VARCHAR,
    cluster VARCHAR,
    visitors_count INTEGER)
ENGINE MergeTree
PRIMARY KEY (timestamp, campus, cluster);
