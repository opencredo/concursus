CREATE KEYSPACE IF NOT EXISTS Concourse
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 2 };

CREATE TABLE IF NOT EXISTS Concourse.Event (
   aggregateType text,
   aggregateId uuid,
   eventTimestamp timestamp,
   streamId text,
   processingId timeuuid,
   name text,
   version text,
   parameters map<text, text>,
   PRIMARY KEY((aggregateType, aggregateId), eventTimestamp, streamId)
) WITH CLUSTERING ORDER BY (eventTimestamp ASC);
