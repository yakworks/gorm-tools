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
