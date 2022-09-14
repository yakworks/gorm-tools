import yakworks.commons.util.BuildSupport

grails.config.locations =  ["classpath:yakworks/test-config.groovy"]

String projectRoot = BuildSupport.gradleRootProjectDir

app {
    resources {
        rootLocation = "${projectRoot}/examples/resources"
        tempDir = "./build/rootLocation/tempDir"
        attachments.location = 'attachments'
    }
}
