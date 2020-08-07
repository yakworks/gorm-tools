/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.hibernate.MappingException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.Type

import gorm.tools.beans.AppCtx
import gorm.tools.idgen.IdGenerator

/**
 * A hibernate IdentifierGenerator that uses a Spring Bean ("idGenerator" is default) to get the id's.
 * beanName defaults to 'idGenerator' and keyName will be "${TableName}.${id_column}".
 * example config.groovy will set the defaults for all domains:
 * <pre>
 * {@code
 *   gorm.default.mapping = {
 *     id generator:'gorm.tools.hibernate.SpringBeanIdGenerator'
 *   }
 * }
 * </pre>
 *
 * can also be sdt in the domain mapping. Example with custom bean name and key name.
 * <pre>
 * {@code
 *   static mapping = {
 *     id generator:'gorm.tools.hibernate.SpringBeanIdGenerator',
 *        params:[beanName:'specialIdGen', keyName:'Org.uuid']
 *   }
 * }
 * </pre>
 * @author Joshua Burnett
 * @since 1.0
 */
@Slf4j
@CompileStatic
class SpringBeanIdGenerator implements IdentifierGenerator, org.hibernate.id.Configurable {
    // Property names for configure() params.
    static final String TARGET_TABLE = "target_table"
    static final String TARGET_COLUMN = "target_column"
    static final String BEAN_NAME = "beanName"
    static final String KEY_NAME = "keyName"

    private String keyName
    private String idGeneratorBeanName
    private IdGenerator idGenerator

    @Override
    void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
        keyName = params.getProperty(KEY_NAME) ?: "${params.getProperty(TARGET_TABLE)}.${params.getProperty(TARGET_COLUMN)}"
        idGeneratorBeanName = params.getProperty(BEAN_NAME)?:'idGenerator'
        //println "SpringBeanIdGenerator.configure params: $params and segmentValue: $segmentValue and type: ${type.name}"
        if (log.isDebugEnabled())
            log.debug("SpringBeanIdGenerator segmentValue: $keyName with params: $params")
    }

    Serializable generate(SharedSessionContractImplementor session, Object obj) {
        // println "obj.id ${obj['id']}"
        if(idGenerator == null) idGenerator = AppCtx.get(idGeneratorBeanName, IdGenerator)
        // if the object has an assigned id then use it.
        Long id = obj['id'] ? (Long)obj['id'] : idGenerator.getNextId(keyName)
        return id
    }

}
