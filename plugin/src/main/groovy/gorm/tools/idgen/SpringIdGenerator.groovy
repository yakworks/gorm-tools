package gorm.tools.idgen

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hibernate.MappingException
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.id.IdentifierGenerator
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.Type
import org.springframework.beans.factory.annotation.Configurable

/** This is a hibernate/spring wrapper that acts as a proxy to the normal IdGenerator generic implementation. */
@Configurable
@Slf4j
@CompileStatic
class SpringIdGenerator implements IdentifierGenerator, org.hibernate.id.Configurable {
    // Property names for configure() params.
    static final String TARGET_TABLE = "target_table"
    static final String TARGET_COLUMN = "target_column"

    /**
     * The configuration parameter holding the table name for the
     * generated id
     */
    static final String TABLE = "target_table"

    private String segmentValue

    @Override
    void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        segmentValue = "${params.getProperty(TARGET_TABLE)}.${params.getProperty(TARGET_COLUMN)}"

        if (log.isDebugEnabled())
            log.debug("SpringIdGenerator configure " + segmentValue + "\t\tidGenerator:" + (IdGeneratorHolder.idGenerator == null ? "null! " : "not null. "))
    }

    Serializable generate(final SessionImplementor session, Object obj) {
        Long id = (Long) IdGeneratorHolder.idGenerator.getNextId(segmentValue)
        //println "${obj.class.name} $id"
        return id
    }

}
