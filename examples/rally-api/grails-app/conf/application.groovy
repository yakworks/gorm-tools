import yakworks.commons.util.BuildSupport

// securityConfig << "classpath:security/security-config.groovy"

// NOTE: reminder that anything in config.locations wins and ovewrites whats in this config
grails.config.locations = [
    "classpath:security.groovy", //comment this out to turn off security
    "classpath:restapi-config.yml",
    "classpath*:restapi/rally/*.yml"
]

//default security
grails.plugin.springsecurity.rest.active = false
grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
grails.plugin.springsecurity.interceptUrlMap = [
    // all accesible anoymously by default
    [pattern: '/**', access: ['IS_AUTHENTICATED_ANONYMOUSLY']]
]

String projectRoot = BuildSupport.gradleRootProjectDir
app {
    resources {
        rootLocation = "${projectRoot}/examples/resources"
        tempDir = "./build/rootLocation/tempDir"
        attachments.location = 'attachments'
    }
}
