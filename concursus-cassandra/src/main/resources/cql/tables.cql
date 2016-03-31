CREATE KEYSPACE IF NOT EXISTS concursus
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };

CREATE TABLE IF NOT EXISTS concursus.Event (
   aggregateType text,
   aggregateId uuid,
   eventTimestamp timestamp,
   streamId text,
   processingId timeuuid,
   name text,
   version text,
   parameters map<text, text>,
   characteristics int,
   PRIMARY KEY((aggregateType, aggregateId), eventTimestamp, streamId)
) WITH CLUSTERING ORDER BY (eventTimestamp DESC);

CREATE TABLE IF NOT EXISTS concursus.Catalogue (
    aggregateType text,
    bucket int,
    aggregateId uuid,
    PRIMARY KEY ((aggregateType, bucket), aggregateId)
) WITH CLUSTERING ORDER BY (aggregateId DESC);
