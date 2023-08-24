/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.jdbc

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException

import groovy.transform.CompileStatic

import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.support.JdbcUtils

import yakworks.commons.map.LazyPathKeyMap

/**
 * Row mapper which allows to convert data from a given ResultSet instance
 * to a grails parameter map, which can be used for databinding.
 */
@SuppressWarnings('JdbcResultSetReference')
@CompileStatic
class PathKeyMapRowMapper extends ColumnMapRowMapper {

    /**
     * Returns a PathKeyMap instance which is build from records in a given ResultSet.
     *
     * @param rs the ResultSet
     * @param rowNum number of records to include to the map
     * @return the map which is build from records in the given ResultSet
     * @throws SQLException
     */
    @Override
    Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData()
        int columnCount = rsmd.getColumnCount()
        Map mapOfColValues = this.createColumnMap(columnCount)

        for (int i = 1; i <= columnCount; ++i) {
            String key = this.getColumnKey(JdbcUtils.lookupColumnName(rsmd, i))
            Object obj = this.getColumnValue(rs, i)
            if (obj != null) {
                mapOfColValues.put(key, obj)
            }
        }

        return mapOfColValues
    }

    /**
     * Returns a PathKeyMap instance
     */
    @Override
    protected Map createColumnMap(int columnCount) {
        return new LazyPathKeyMap([:])
    }

}
