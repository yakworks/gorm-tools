package gorm.tools.idgen

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hibernate.MappingException
import org.hibernate.dialect.Dialect
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.id.IdentifierGenerator
import org.hibernate.type.Type
import org.springframework.beans.factory.annotation.Configurable

/** This is a hibernate/spring wrapper that acts as a proxy to the normal IdGenerator generic implementation. */
@Configurable
@Slf4j
@CompileStatic
public class SpringIdGenerator implements IdentifierGenerator, org.hibernate.id.Configurable {
	// Property names for configure() params.
	public static final String PROP_ENTITY="entity_name"
	public static final String PROP_TABLE="target_table"
	public static final String PROP_ID_TABLE="identity_tables"
	public static final String PROP_COLUMN="target_column"

	/**
	 * The configuration parameter holding the table name for the
	 * generated id
	 */
	public static final String TABLE = "target_table"

	private String segment_value
	
	public SpringIdGenerator() {
	}

	public void configure(Type type, Properties params, Dialect dialect)  throws MappingException {
		//this.params = params;
		segment_value = params.getProperty( TABLE ) + ".id "
		showProperties("SpringIdGenerator configure ")
	}
	
	private void showProperties(String prefix) {
		if(log.isDebugEnabled()) {
			log.debug(prefix + segment_value + "\t\tidGenerator:" + (IdGeneratorHolder.idGenerator==null?"null! ":"not null. "))
		}
	}

	public synchronized Serializable generate(final SessionImplementor session, Object obj) {
		showProperties("SpringIdGenerator.generate ")
		return (Long) IdGeneratorHolder.idGenerator.getNextId(segment_value)
	}

}
