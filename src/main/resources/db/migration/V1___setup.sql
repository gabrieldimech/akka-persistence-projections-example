CREATE TABLE IF NOT EXISTS news_articles (
    id          VARCHAR(100) PRIMARY KEY NOT NULL,
    description VARCHAR(100) NOT NULL,
    title       VARCHAR(200) NOT NULL,
    link        VARCHAR(200) NOT NULL,
    topic       VARCHAR(100) NOT NULL,
    pub_date    timestamp    NOT NULL
);

CREATE TABLE IF NOT EXISTS event_journal(
    ordering SERIAL,
    deleted BOOLEAN DEFAULT false NOT NULL,
    persistence_id VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    writer TEXT NOT NULL,
    write_timestamp BIGINT NOT NULL,
    adapter_manifest TEXT NOT NULL,
    event_payload LONGBLOB NOT NULL,
    event_ser_id INTEGER NOT NULL,
    event_ser_manifest TEXT NOT NULL,
    meta_payload BLOB,
    meta_ser_id INTEGER,meta_ser_manifest TEXT,
    PRIMARY KEY(persistence_id,sequence_number)
);

CREATE UNIQUE INDEX event_journal_ordering_idx ON event_journal(ordering);

CREATE TABLE IF NOT EXISTS event_tag (
    event_id BIGINT UNSIGNED NOT NULL,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY(event_id, tag),
    FOREIGN KEY (event_id)
        REFERENCES event_journal(ordering)
        ON DELETE CASCADE);

CREATE TABLE IF NOT EXISTS snapshot (
    persistence_id VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    created BIGINT NOT NULL,
    snapshot_ser_id INTEGER NOT NULL,
    snapshot_ser_manifest TEXT NOT NULL,
    snapshot_payload LONGBLOB NOT NULL,
    meta_ser_id INTEGER,
    meta_ser_manifest TEXT,
    meta_payload BLOB,
    PRIMARY KEY (persistence_id, sequence_number));

CREATE TABLE IF NOT EXISTS akka_projection_offset_store (
  projection_name VARCHAR(255) NOT NULL,
  projection_key VARCHAR(255) NOT NULL,
  current_offset VARCHAR(255) NOT NULL,
  manifest VARCHAR(4) NOT NULL,
  mergeable BOOLEAN NOT NULL,
  last_updated BIGINT NOT NULL,
  PRIMARY KEY(projection_name, projection_key)
);

CREATE INDEX akka_projection_name_index ON akka_projection_offset_store (projection_name);

CREATE TABLE IF NOT EXISTS akka_projection_management (
  projection_name VARCHAR(255) NOT NULL,
  projection_key VARCHAR(255) NOT NULL,
  paused BOOLEAN NOT NULL,
  last_updated BIGINT NOT NULL,
  PRIMARY KEY(projection_name, projection_key)
);

CREATE TABLE IF NOT EXISTS id_mapping (
    id BIGINT auto_increment PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL,
    mapping_type VARCHAR(255) NOT NULL,
    generated_uuid VARCHAR(255) NOT NULL,
    CONSTRAINT id_mapping_external_id_type_UN UNIQUE KEY (external_id, mapping_type),
    CONSTRAINT id_mapping_generated_id_UN UNIQUE KEY (generated_uuid)
);