package gorm.tools.hibernate

import gorm.tools.beans.AppCtx
import gorm.tools.idgen.IdGenerator
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hibernate.MappingException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.Type

/**
 * A hibernate IdentifierGenerator that uses a Spring Bean ("idGenerator" is default) to get the id's.
 *
 * @author Joshua Burnett
 * @since 1.0
 */
@Slf4j
@CompileStatic
class SpringBeanIdGenerator implements IdentifierGenerator, org.hibernate.id.Configurable {
    // Property names for configure() params.
    static final String TARGET_TABLE = "target_table"
    static final String TARGET_COLUMN = "target_column"

    /**
     * The configuration parameter holding the table name for the
     * generated id
     */
    static final String TABLE = "target_table"

    private String segmentValue
    private String idGeneratorBeanName
    private IdGenerator idGenerator

    @Override
    void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        segmentValue = "${params.getProperty(TARGET_TABLE)}.${params.getProperty(TARGET_COLUMN)}"
        idGeneratorBeanName = params.getProperty('beanName')?:'idGenerator'
        //println "SpringBeanIdGenerator.configure params: $params and segmentValue: $segmentValue and type: ${type.name}"
        if (log.isDebugEnabled())
            log.debug("SpringBeanIdGenerator segmentValue: $segmentValue with params: $params")
    }

    Serializable generate(SharedSessionContractImplementor session, Object obj) {
        if(idGenerator == null) idGenerator = AppCtx.get(idGeneratorBeanName, IdGenerator)
        Long id = idGenerator.getNextId(segmentValue)
        return id
    }

}
