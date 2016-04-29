package com.opencredo.concursus.cassandra.events;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.opencredo.concursus.domain.events.cataloguing.AggregateCatalogue;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An {@link AggregateCatalogue} that uses Cassandra for persistence.
 */
public class CassandraAggregateCatalogue implements AggregateCatalogue {

    /**
     * Create a new {@link AggregateCatalogue} that uses Cassandra for persistence.
     * @param cassandraTemplate The {@link CassandraTemplate} to use to execute Cassandra queries.
     * @param bucketCount The number of buckets to use to distribute the catalogue data over multiple rows.
     * @return The constructed {@link AggregateCatalogue}.
     */
    public static AggregateCatalogue create(CassandraTemplate cassandraTemplate, int bucketCount) {
        PreparedStatement insertStatement = cassandraTemplate.getSession().prepare(
                "INSERT INTO Catalogue (aggregateType, bucket, aggregateId) VALUES (?, ?, ?)");
        PreparedStatement deleteStatement = cassandraTemplate.getSession().prepare(
                "DELETE FROM Catalogue WHERE aggregateType = ? AND bucket = ? AND aggregateId = ?");
        return new CassandraAggregateCatalogue(bucketCount, cassandraTemplate, insertStatement, deleteStatement);
    }

    private final int bucketCount;
    private final CassandraTemplate cassandraTemplate;
    private final PreparedStatement insertStatement;
    private final PreparedStatement deleteStatement;

    private CassandraAggregateCatalogue(int bucketCount, CassandraTemplate cassandraTemplate, PreparedStatement insertStatement, PreparedStatement deleteStatement) {
        this.bucketCount = bucketCount;
        this.cassandraTemplate = cassandraTemplate;
        this.insertStatement = insertStatement;
        this.deleteStatement = deleteStatement;
    }

    @Override
    public void add(String aggregateType, String aggregateId) {
        cassandraTemplate.execute(insertStatement.bind(aggregateType, getBucket(aggregateId), aggregateId));
    }

    @Override
    public void remove(String aggregateType, String aggregateId) {
        cassandraTemplate.execute(deleteStatement.bind(aggregateType, getBucket(aggregateId), aggregateId));
    }

    @Override
    public List<String> getAggregateIds(String aggregateType) {
        Select select = QueryBuilder.select("aggregateId").from("Catalogue");
        select.where(QueryBuilder.eq("aggregateType", aggregateType));
        select.where(QueryBuilder.in("bucket", IntStream.range(0, bucketCount).mapToObj(Integer::valueOf).collect(Collectors.toList())));

        List<String> result = new ArrayList<>();
        cassandraTemplate.query(select, (Row row) -> result.add(row.getString(0)));
        return result;
    }

    private int getBucket(String aggregateId) {
        return Math.abs(aggregateId.hashCode()) % bucketCount;
    }
}
