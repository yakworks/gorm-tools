import yakworks.commons.util.BuildSupport

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

grails.config.locations =  ["classpath:yakworks/test-config.groovy"]

String projectRoot = BuildSupport.gradleRootProjectDir

app {
    resources {
        rootLocation = "${projectRoot}/examples/resources"
        tempDir = "./build/rootLocation/tempDir"
        attachments.location = 'attachments'
    }
}
