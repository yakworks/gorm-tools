
grails {
    gorm.failOnError = true
    gorm.default.mapping = {
        id generator: 'gorm.tools.hibernate.SpringBeanIdGenerator'
        // '*'(cascadeValidate: 'dirty')
        //cache usage: System.getProperty("cacheStrategy", "read-write").toString()
    }
    gorm.default.constraints = {
        '*'(nullable:true)
    }
}

grails.config.locations =  ["classpath:yakworks/test-config.groovy"]
