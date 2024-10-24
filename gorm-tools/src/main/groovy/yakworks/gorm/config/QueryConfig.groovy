/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

import yakworks.spring.config.Enabler

@ConfigurationProperties(prefix="yakworks.gorm.query")
@CompileStatic
class QueryConfig {

    /** Max items, user cannot pass in a max higher that this, default is 100 */
    Integer max = 100

    /** Query timeout in seconds, default is 30 */
    Integer timeout = 30

    /**
     * Max value allowed for excel exports
     * Default to 1000, RNDC can have 10K
     */
    Integer exportMax = 1000

    /**
     * if true will use the custom fn_ilike in the JpqlQueryBuilder
     */
    Enabler dialectFunctions = new Enabler(enabled: false)
}
