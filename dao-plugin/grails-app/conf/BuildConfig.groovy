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
        build(":release:3.1.2", ":rest-client-builder:2.1.1") { export = false }
        compile (':spring-security-core:2.0-RC4'){ export = false }
        compile (":hibernate4:4.3.10") { export = false }
    }
}
