
CREATE TABLE visitors_transit_log (
    timestamp TIMESTAMP,
    campus VARCHAR,
    cluster VARCHAR,
    place VARCHAR(2),
    login VARCHAR,
    direction VARCHAR)
ENGINE MergeTree
PRIMARY KEY (timestamp, login);
