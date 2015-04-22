grails.project.work.dir = 'target'
grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsPlugins()
        grailsCentral()

        mavenLocal()
        mavenCentral()
    }

    plugins {
        build (":release:3.1.1", ":rest-client-builder:1.0.3") { export = false }
        compile (':spring-security-core:2.0-RC4'){
            export = false
        }
        runtime(":hibernate:3.6.10.18") { export = false }
    }
}
