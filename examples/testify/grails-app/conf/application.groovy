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

// gorm.tools.async.enabled = true

//gorm.tools.audit.enabled = false

grails.config.locations =  ["classpath:yakworks/test-config.groovy"]

String projectRoot = BuildSupport.gradleRootProjectDir
app {
    resources {
        currentTenant = {
            return [num: 'virgin', id: 2]
        }
        rootLocation = { args ->
            File root = new File("${projectRoot}/examples/resources")
            return root.canonicalPath
        }
        tempDir = {
            File file = new File("./build/rootLocation/tempDir")
            if (!file.exists()) file.mkdirs()
            return file.canonicalPath
        }
        attachments.location = 'attachments'
    }
}
