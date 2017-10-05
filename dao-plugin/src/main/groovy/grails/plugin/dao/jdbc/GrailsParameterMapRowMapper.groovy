package grails.plugin.dao.jdbc

import groovy.transform.CompileStatic
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.support.JdbcUtils
import org.springframework.mock.web.MockHttpServletRequest

import javax.servlet.http.HttpServletRequest
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException

/**
 * Row mapper which converts the resultset to grails parameter map, which can be used for databinding
 */
@CompileStatic
class GrailsParameterMapRowMapper extends ColumnMapRowMapper {

	@Override
	public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
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

	@Override
	protected Map<String, Object> createColumnMap(int columnCount) {
		HttpServletRequest request = new MockHttpServletRequest()
		return new GrailsParameterMap(request)
	}

}
