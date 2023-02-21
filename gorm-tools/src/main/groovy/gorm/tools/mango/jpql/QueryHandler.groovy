package gorm.tools.mango.jpql

import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.query.Query
import org.springframework.core.convert.ConversionService

/**
 * simple interface to be implemented for each Query. type
 */
interface QueryHandler {
    int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                      String logicalName, int position, List parameters, ConversionService conversionService)

}
