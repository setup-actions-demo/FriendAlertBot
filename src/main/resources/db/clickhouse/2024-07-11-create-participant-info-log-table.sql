
CREATE TABLE participant_info_log (
    login VARCHAR,
    class_name VARCHAR,
    parallel_name VARCHAR,
    exp_value INTEGER,
    level INTEGER,
    exp_to_next_level INTEGER,
    campus VARCHAR,
    status VARCHAR,
    updated_at TIMESTAMP)
ENGINE MergeTree
PRIMARY KEY (login);
