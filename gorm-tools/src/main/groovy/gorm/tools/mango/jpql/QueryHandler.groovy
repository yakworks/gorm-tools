/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.jpql

import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.query.Query
import org.springframework.core.convert.ConversionService

/**
 * simple interface to be implemented for each Query. type
 */
interface QueryHandler {
    @SuppressWarnings(['ParameterCount'])
    int handle(PersistentEntity entity, Query.Criterion criterion, StringBuilder q, StringBuilder whereClause,
                      String logicalName, int position, List parameters, ConversionService conversionService)

}
