/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.idgen

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Propagation

import grails.gorm.transactions.Transactional
import yakworks.commons.lang.Validate
import yakworks.gorm.config.IdGeneratorConfig

//import grails.gorm.transactions.Transactional
/**
 * A Jdbc implementation of the IdGenerator. Will query a central table for new ids.
 * defaults to the following but can be set accordingly:
 *  table - "NEWOBJECTID"
 *  keyColumn - "KeyName"
 *  idColumn - "NextId"
 *  seedValue - "1000" this is the starting ID fi the row does not exist and is created by this object.
 *
 *  setCreateIdRow = false to not create the row automatically if it does not exist
 *
 * @author Joshua Burnett (@basejump)
 * @since 1.0
 */
@Slf4j
@CompileStatic
class JdbcIdGenerator implements IdGenerator {

    /** Max attempts when another transaction/pod wins the optimistic key update. */
    private static final int MAX_RETRIES = 5

    @Autowired JdbcTemplate jdbcTemplate
    @Autowired IdGeneratorConfig idGenConfig



    //if true then will not automatically create a row for the key and will throw an error if row does not exist
    boolean requireKeyRow = false

    String table = "NewObjectId"
    String keyColumn = "KeyName"
    String idColumn = "NextId"

    long getNextId(String keyName) {
        throw new IllegalAccessException("Use the pooledIdGenerator with this backing it for fetching single IDs")
    }

    @SuppressWarnings('SynchronizedMethod')
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    synchronized long getNextId(String keyName, long increment) {
        return updateIncrement(keyName, increment)
    }

    /**
     * Allocates a batch of ids for "key name" returns the first id in the range and advances {@code NextId} by
     * {@code increment}. Uses optimistic concurrency
     *
     * Transactional!  The annotation only works on public methods, so this method should only be called by transactional methods.
     */
    private long updateIncrement(String name, long increment) {
        Validate.notEmpty(idColumn, 'idColumn')
        Validate.notEmpty(keyColumn, 'keyColumn')
        Validate.notEmpty(table, 'table')
        Validate.notEmpty(name, 'name argument')


        //Reads NextId and tries to update it like
        //Update NewObjectId set NextId = Xxx where KeyName ='Customer.id' and NextId = <old-value>
        //if another pod had already updated it by the time between read and update, thn the updated row could will be 0
        //because where NextId = <old-value> will not match in this case we retry till max attempts
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String query = "Select " + idColumn + " from " + table + " where " + keyColumn + " ='" + name + "'"
            long oid = 0
            try {
                oid = jdbcTemplate.queryForObject(query, Long)
            } catch (EmptyResultDataAccessException erdax) {
                if (requireKeyRow) {
                    throw erdax
                } else {
                    oid = createRow(table, keyColumn, idColumn, name)
                }
            } catch (BadSqlGrammarException bge) {
                log.info("Looks like the idgen table is not found. This will do a automatically setup for the table for the JdbcIdGenerator "+
                    "suggested to set it up properly with something like db-migration"+
                    "or another tools as no indexes or optimization are taken into account")
                createTable(table, keyColumn, idColumn)
                oid = createRow(table, keyColumn, idColumn, name)
                //throw new IllegalArgumentException("The key '" + name + "' does not exist in the object ID table.");
            }

            if (oid > 0) { //found it
                long dbOid = oid // value read from db; used in WHERE for optimistic update
                if (oid < idGenConfig.startValue) {
                    oid = idGenConfig.startValue
                }
                long newValue = oid + increment
                // only update if NextId is still dbOid — otherwise another pod/trx had already updated it and we retry
                int updated = jdbcTemplate.update("Update " + table + " set " + idColumn + " = " + newValue + " where " + keyColumn
                    + " ='" + name + "' and " + idColumn + " = " + dbOid)
                if (updated == 1) {
                    if (log.isDebugEnabled()) {
                        log.debug("Returning id " + oid + " for key '" + name + "'")
                    }
                    return oid
                } else {
                    log.warn("Concurrent id batch allocation for key '" + name + "' - another transaction/pod updated " + idColumn +
                        " since read " + dbOid + "; would have returned duplicate ids without optimistic update. " +
                        "Retrying attempt " + (attempt + 1) + " of " + MAX_RETRIES)
                }
            }
        }
        //if all retries are over, should be very rare
        throw new IllegalStateException("Failed to allocate id batch for key '" + name + "' after " + MAX_RETRIES + " attempts")
    }

    private long createRow(String table, String keyColumn, String idColumn, String name) {
        Long maxId = idGenConfig.startValue
        String[] tableInfo = name.split("\\.")
        if (tableInfo.length > 1) {
            try {
                String maxSql = "select max(" + tableInfo[1] + ") from " + tableInfo[0]
                Long currentMax = jdbcTemplate.queryForObject(maxSql, Long)
                if (currentMax != null) maxId = currentMax + 1
            } catch (EmptyResultDataAccessException ex) {
                log.debug("No rows yet so just leave it as seed. TableInfo=${tableInfo}")
                //now rows yet so just leave it as  seed
            }
        }
        jdbcTemplate.update("insert into " + table + " (" + keyColumn + "," + idColumn + ") " + "Values('" + name + "'," + maxId + ")")
        return maxId
    }

    private void createTable(String table, String keyColumn, String idColumn) {
        String query = """
            create table $table
                (
                    $keyColumn varchar(255) not null,
                    $idColumn bigint not null,
                    CONSTRAINT PK_$table PRIMARY KEY ($keyColumn)
                )
                """
        //println "creating with $query"
        jdbcTemplate.execute(query)
    }
}
