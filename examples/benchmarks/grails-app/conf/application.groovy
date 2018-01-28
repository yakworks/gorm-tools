grails {
    plugin {
        audittrail {
            enabled = Boolean.valueOf(System.getProperty("auditTrailEnabled", "false"))
            //For a field to be added by the annotation at least on config setting needs to be present for that field.
            createdBy.field = "createdBy"  // createdBy is default
            createdBy.constraints = "nullable:false,display:false,editable:false,bindable:false"
            createdDate.field = "createdDate"
            createdDate.constraints = "nullable:false,display:false,editable:false,bindable:false"

            editedBy.field = "editedBy"  // createdBy is default
            editedBy.constraints = "nullable:false,display:false,editable:false,bindable:false"
            editedDate.field = "editedDate"
            editedDate.constraints = "nullable:false,display:false,editable:false,bindable:false"
        }
    }
}

grails.gorm.autowire = Boolean.valueOf(System.getProperty("autowire.enabled", "false"))
hibernate.jdbc.batch_size = System.getProperty("jdbcBatchSize", "100").toInteger()
gpars.poolsize = 5

/** Custome config for benchmarks **/

benchmark {
    //#items per transation. used to collate the rows into lists of lists of batchSliceSize.
    def bss = System.getProperty("batchSliceSize", "0").toInteger()
    bss = bss ?: System.getProperty("jdbcBatchSize", "100").toInteger()
    batchSliceSize = bss
    //number of times to load the file of 36k rows. the default of 3 is equal to 111k rows for example
    loadIterations = System.getProperty("loadIterations", "1").toInteger()
    //number of refreshable listeners to register to simulate load on performance
    eventListenerCount = System.getProperty("eventListenerCount", "0").toInteger()
    eventSubscriberCount = System.getProperty("eventSubscriberCount", "0").toInteger()
    binder.type = System.getProperty("binderType", "fast")
}

grails {
    gorm.default.mapping = {
        id generator: 'gorm.tools.hibernate.PooledTableIdGenerator'
        cache usage: System.getProperty("cacheStrategy", "read-write").toString()
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
