// NOTE: THIS IS ALL HERE FOR TESTING/DEV RALLY.
// REMINDER THAT application.groovy DOES NOT GET DEPLOYED OR USED IN LIBS THAT DEPEND ON THIS

// grails.profile = "plugin"
// grails.codegen.defaultPackage = "grails.plugin.repository"
// grails.full.stacktrace = true
// grails.show.stacktrace = true
// grails.verbose = true

grails.config.locations =  [
    "classpath:core_configs/datasource-config.groovy",
    "classpath:core_configs/nine-resource-defaults.groovy",
    "classpath:dev_configs/rally-test-config.groovy",
    "classpath:dev_configs/security-test-config.groovy",
    // "file:~/.grails/9ci-settings.groovy"
]
