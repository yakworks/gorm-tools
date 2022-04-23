//standard gorm defaults for id generator that can be added to grails.config.locations
grails {
    //gorm.flushMode = 'AUTO'
    gorm.failOnError = true
    gorm.default.mapping = {
        id generator: 'gorm.tools.hibernate.SpringBeanIdGenerator'
        '*'(cascadeValidate: 'dirty')
    }
    gorm.default.constraints = {
        '*'(nullable: true)
    }
}
