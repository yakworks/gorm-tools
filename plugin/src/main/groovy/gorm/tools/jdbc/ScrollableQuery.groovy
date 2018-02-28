/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.jdbc

import groovy.sql.Sql
import groovy.transform.CompileStatic
import org.springframework.jdbc.core.RowMapper

import javax.sql.DataSource
import java.sql.ResultSet
import java.sql.Statement

/**
 * Groovy Sql wrapper for running scrollable/streaming queries.
 * It provides a convenient way for iterating over records which correspond to a given sql query.
 */
@SuppressWarnings(['JdbcResultSetReference', 'JdbcStatementReference'])
@CompileStatic
class ScrollableQuery {

    private DataSource dataSource
    private RowMapper rowMapper
    private int fetchSize

    /**
     * Creates a ScrollableQuery instance with given parameters.
     *
     * @param mapper a row mapper
     * @param dataSource a datasource
     * @param fetchSize the number of result set rows
     */
    ScrollableQuery(RowMapper mapper, DataSource dataSource, int fetchSize = Integer.MIN_VALUE) {
        this.dataSource = dataSource
        this.rowMapper = mapper
        this.fetchSize = fetchSize
    }

    /**
     * Executes the query and calls the closure for each row.
     *
     * @param query an sql query which represents records
     * @param closure a closure which is called for each row
     */
    void eachRow(String query, Closure closure) {
        Sql sql = prepareSql()
        int index = 1

        sql.query(query) { ResultSet r ->
            while (r.next()) {
                index++
                Object row = rowMapper.mapRow(r, index)
                closure.call(row)
            }
        }
    }

    /**
     * Executes the query and calls the closure for each batch of records.
     *
     * For example, the next snippet of code prints records in batch and its size for every 10 records:
     *  eachBatch(sqlQuery, 10) { List batch ->
     *      println batch
     *      println batch.size()
     *}*
     * @param query an sql query which represents records
     * @param batchSize number of records in a single batch
     * @param closure a closure which is called for each batch
     */
    void eachBatch(String query, int batchSize, Closure cl) {
        List batch = []
        this.eachRow(query) { Object row ->
            batch.add(row)
            if ((batch.size() == batchSize)) {
                cl.call(batch)
                batch = []
            }
        }
        //there could be remaning rows
        if (batch.size() > 0) cl.call(batch)
    }

    /**
     * Load all rows and return as a list, each row in the list would be converted using the passed RowMapper
     * This method holds all rows in memory, so this should not be used if there is going to be large number of rows.
     * instead use the eachRow, eachBatch which works with the scrollable resultset
     *
     * @param query an sql query which represents records
     * @return a list of records which correspond to the given query
     */
    List rows(String query) {
        List result = []

        this.eachRow(query) { Object row ->
            result.add(row)
        }

        return result
    }

    protected Sql prepareSql() {
        Sql sql = new Sql(dataSource)
        sql.resultSetConcurrency = java.sql.ResultSet.CONCUR_READ_ONLY
        sql.withStatement { Statement stmt -> stmt.fetchSize = fetchSize }
        return sql
    }

}
