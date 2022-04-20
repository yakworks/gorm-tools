// THIS IS ALL HERE FOR TESTING/DEV RALLY.. Reminder, DOES NOT GET DEPLOYED ANYWHERE OVERRIDES Plugin.groovy configs
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

grails.config.locations = ["classpath:api.yml"]

grails.config.locations =  [
    // "classpath:core_configs/datasource-config.groovy",
    // "classpath:core_configs/nine-resource-defaults.groovy",
    // "classpath:dev_configs/rally-test-config.groovy",
    "classpath:dev_configs/security-test-config.groovy",
    // "file:~/.grails/9ci-settings.groovy"
]
