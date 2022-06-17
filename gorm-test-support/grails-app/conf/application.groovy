// FOR TESTING ONLY, reminder that nothing here get published with jar
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
app {
    resources {
        rootLocation = "./build/rootLocation/"
        tempDir = "./build/rootLocation/tempDir"
        attachments.location = 'attachments'
    }
}
