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

// grails.config.locations = ["classpath:api.yml", "classpath:gorm-tools/defaults.groovy"]

// This part is only applicable to rally-domain and rally-spring but its doesnt hurt that the other plugins get it
app {
    resources {
        currentTenant = {
            return [num: 'virgin', id: 2]
        }
        rootLocation = { args ->
            File file = new File("./build/rootLocation/${args.tenantSubDomain}-${args.tenantId}")
            if (!file.exists()) file.mkdirs()
            return file.canonicalPath
        }
        tempDir = {
            File file = new File("./build/rootLocation/tempDir")
            if (!file.exists()) file.mkdirs()
            return file.canonicalPath
        }
        attachments.location = 'attachments'
    }
}
