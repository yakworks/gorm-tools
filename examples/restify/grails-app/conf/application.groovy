grails {
    gorm.failOnError = true
    gorm.default.mapping = {
        id generator: 'gorm.tools.hibernate.SpringBeanIdGenerator'
    }
    gorm.default.constraints = {
        '*'(nullable:true)
    }
}
//grails.plugin.fields.disableLookupCache = true
//grails.converters.domain.include.version = true
