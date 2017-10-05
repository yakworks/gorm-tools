package gorm.tools.idgen

import org.apache.log4j.Category
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Propagation
import grails.transaction.Transactional
import groovy.transform.CompileStatic

/**
 * A Jdbc implementation of the IdGenerator. Will query a central table for new ids.
 * defaults to the following but can be set accordingly:
 * 	table - "NEWOBJECTID"
 * 	keyColumn - "KeyName"
 * 	idColumn - "NextId"
 * 	seedValue - "1000" this is the starting ID fi the row does not exist and is created by this object.
 *
 *	setCreateIdRow = false to not create the row automatically if it does not exist
 * @author Josh
 *
 */
//FIXME Sud - do we need tests here
@CompileStatic
public class JdbcIdGenerator implements IdGenerator {
	private static Category log = Category.getInstance(JdbcIdGenerator.class)
	JdbcTemplate jdbcTemplate

	private long seedValue = 1000 //the Id to start with if it does not exist in the table
	private String table = "NEWOBJECTID"
	private String keyColumn = "KeyName"
	private String idColumn = "NextId"

	public JdbcIdGenerator() {
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public long getNextId(String keyName){
		return getNextId(keyName, 1)
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public synchronized long getNextId(String keyName, long increment){
		long oid = updateIncrement(keyName,increment)
		// if (oid==0) { //no rows exist so create it
		// 	oid = seedValue;
		// 	long futureId = seedValue+increment;
		// 	jdbcTemplate.update("insert into "+table+" ("+keyColumn+"," + idColumn + ") "+
		// 			"Values('" +keyName+"'," + futureId + ")" );
		// }
		return oid
	}

	// Transactional!  The annotation only works on public methods, so this method should only be called by transactional
	// methods.
	private long updateIncrement(String name,long increment){
		if(name==null || name.length()==0){
			if(log.isDebugEnabled()) {
//				log.debug("Returning empty ID for empty key!");
//				if(log.isTraceEnabled()) {
					log.debug(new Exception("Returning empty ID for empty key!"))
//				}
			}
			return 0
		}else{
			if(idColumn == null) throw new NullPointerException("Can't get a new row ID because the identity column name is not set!")
			if(table == null) throw new NullPointerException("Can't get a new row ID because the identity table is not set!")
			if(keyColumn == null) throw new NullPointerException("Can't get a new row ID because the key column is not set!")
			if(name==null) throw new NullPointerException("Can't get a new row ID because the requesting object's table name is not set!")
			if(name.length() == 0) throw new IllegalArgumentException("Can't get a row ID because the requesting object's table name has zero length!")

			String query = "Select " + idColumn + " from " + table + " where "+keyColumn+" ='" + name + "'"
			long oid = 0
			try {
				oid = jdbcTemplate.queryForObject(query, Long)
			} catch(EmptyResultDataAccessException erdax) {
				oid = createRow( table, keyColumn,  idColumn,  name)
				//throw new IllegalArgumentException("The key '" + name + "' does not exist in the object ID table.")
			}
			if (oid>0) { //found it
				if(oid < seedValue) {
					oid = seedValue
				}
				long newValue = oid + increment
				jdbcTemplate.update("Update " + table + " set " + idColumn + " = " + newValue + " where " + keyColumn
						+ " ='" + name + "'")
			}
			if(log.isDebugEnabled()) {
				log.debug("Returning id " + oid + " for key '" + name + "'")
			}
			return oid
		}
	}

	private long createRow(String table,String keyColumn, String idColumn, String name){
		Long maxId= seedValue
		String[] tableInfo  = name.split("\\.")
		if(tableInfo.length>1){
			try {
				String maxSql = "select max("+tableInfo[1]+") from " + tableInfo[0]
				Long currentMax = jdbcTemplate.queryForObject(maxSql, Long)
				if(currentMax != null) maxId = currentMax + 1
			} catch(EmptyResultDataAccessException ex) {
				log.debug("No rows yet so just leave it as seed. TableInfo=${tableInfo}")
				//now rows yet so just leave it as  seed
			}
		}
		jdbcTemplate.update("insert into "+table+" ("+keyColumn+"," + idColumn + ") "+ "Values('" +name+"'," + maxId + ")" )
		return maxId
	}

	public String getKeyColumn() {
		return keyColumn
	}

	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn
	}

	public long getSeedValue() {
		return seedValue
	}

	public void setSeedValue(long seedValue) {
		this.seedValue = seedValue
	}

	public String getTable() {
		return table
	}

	public void setTable(String table) {
		this.table = table
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate
	}

	public String getIdColumn() {
		return idColumn
	}

	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn
	}

}
