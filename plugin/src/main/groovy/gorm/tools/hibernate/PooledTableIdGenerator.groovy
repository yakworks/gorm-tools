package gorm.tools.hibernate

import gorm.tools.beans.AppCtx
import gorm.tools.idgen.IdGenerator
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hibernate.MappingException
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.Type

@Slf4j
@CompileStatic
class PooledTableIdGenerator implements IdentifierGenerator, org.hibernate.id.Configurable {
    // Property names for configure() params.
    static final String TARGET_TABLE = "target_table"
    static final String TARGET_COLUMN = "target_column"

    /**
     * The configuration parameter holding the table name for the
     * generated id
     */
    static final String TABLE = "target_table"

    private String segmentValue
    private IdGenerator idGenerator

    @Override
    void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        segmentValue = "${params.getProperty(TARGET_TABLE)}.${params.getProperty(TARGET_COLUMN)}"
        //idGenerator = AppCtx.get("idGenerator",IdGenerator)
        //println "PooledTableIdGenerator.configure params: $params and segmentValue: $segmentValue and type: ${type.name}"
        if (log.isDebugEnabled())
            log.debug("PooledTableIdGenerator segmentValue: $segmentValue with params: $params")
    }

    Serializable generate(SharedSessionContractImplementor session, Object obj) {
        if(idGenerator == null) idGenerator = AppCtx.get("idGenerator", IdGenerator)
        Long id = idGenerator.getNextId(segmentValue)
        return id
    }

    //public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException;

}
