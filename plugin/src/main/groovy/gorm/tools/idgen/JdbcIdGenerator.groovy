/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.idgen

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.commons.lang.Validate
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.BadSqlGrammarException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Propagation

import grails.gorm.transactions.Transactional

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
 *
 */
@Slf4j
@CompileStatic
class JdbcIdGenerator implements IdGenerator {
    JdbcTemplate jdbcTemplate

    @Value('${gorm.tools.idGenerator.seedValue:1000}')
    long seedValue//the Id to start with if it does not exist in the table

    String table = "NEWOBJECTID"
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

    // Transactional!  The annotation only works on public methods, so this method should only be called by transactional
    // methods.
    private long updateIncrement(String name, long increment) {
        //println "updateIncrement $name $increment"
        Validate.notNull(idColumn, "The idColumn is undefined")
        Validate.notNull(keyColumn, "The keyColumn is undefined")
        Validate.notNull(table, "The table is undefined")
        Validate.notNull(name, "The name is undefined")
        Validate.notEmpty(name, "The name is empty")

        String query = "Select " + idColumn + " from " + table + " where " + keyColumn + " ='" + name + "'"
        long oid = 0
        try {
            oid = jdbcTemplate.queryForObject(query, Long)
        } catch (EmptyResultDataAccessException erdax) {
            oid = createRow(table, keyColumn, idColumn, name)
            //throw new IllegalArgumentException("The key '" + name + "' does not exist in the object ID table.")
        } catch (BadSqlGrammarException bge) {
            log.warn("Looks like the idgen table is not found. This will do a dirty setup for the table for the JdbcIdGenerator "+
                "for testing apps but its STRONGLY suggested you set it up properly with something like db-migration"+
                "or another tools as not indexes or optimization are taken into account")
            createTable(table, keyColumn, idColumn)
            oid = createRow(table, keyColumn, idColumn, name)
            //throw new IllegalArgumentException("The key '" + name + "' does not exist in the object ID table.");
        }

        if (oid > 0) { //found it
            if (oid < seedValue) {
                oid = seedValue
            }
            long newValue = oid + increment
            jdbcTemplate.update("Update " + table + " set " + idColumn + " = " + newValue + " where " + keyColumn
                + " ='" + name + "'")
        }
        if (log.isDebugEnabled()) {
            log.debug("Returning id " + oid + " for key '" + name + "'")
        }
        return oid
    }

    private long createRow(String table, String keyColumn, String idColumn, String name) {
        Long maxId = seedValue
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
