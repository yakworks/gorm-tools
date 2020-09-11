gorm.tools.security.audit.enabled = Boolean.valueOf(System.getProperty("auditTrailEnabled", "true"))
// grails.plugin.audittrail.enabled = false

grails.gorm.autowire = Boolean.valueOf(System.getProperty("autowire.enabled", "false"))
hibernate.jdbc.batch_size = System.getProperty("jdbcBatchSize", "255").toInteger()
gpars.poolsize = 4

/** Custome config for benchmarks **/

benchmark {
    //#items per transation. used to collate the rows into lists of lists of batchSliceSize.
    def bss = System.getProperty("batchSliceSize", "0").toInteger()
    bss = bss ?: System.getProperty("jdbcBatchSize", "255").toInteger()
    batchSliceSize = bss
    //number of times to load the file of 36k rows. the default of 3 is equal to 111k rows for example
    multiplyData = System.getProperty("multiplyData", "3").toInteger()
    //number of refreshable listeners to register to simulate load on performance
    eventListenerCount = System.getProperty("eventListenerCount", "0").toInteger()
    eventSubscriberCount = System.getProperty("eventSubscriberCount", "0").toInteger()
    binder.type = System.getProperty("binderType", "gorm-tools")
}

grails {
    gorm.failOnError = true
    gorm.default.mapping = {
        id generator: 'gorm.tools.hibernate.SpringBeanIdGenerator'
        '*'(cascadeValidate: 'dirty')
        //cache usage: System.getProperty("cacheStrategy", "read-write").toString()
    }
    gorm.default.constraints = {
        '*'(nullable:true)
    }
}

grails.plugin.springsecurity.active = Boolean.valueOf(System.getProperty("springsecurity.active", "true"))

environments {
    production {
        grails.dbconsole.enabled = true
    }
}

hibernate {
    cache {
        use_second_level_cache = System.getProperty("secondLevelCache", "false").toBoolean()
    }
}
