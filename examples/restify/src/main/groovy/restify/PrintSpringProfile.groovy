package restify

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * Experiments with profiles
 */
@Component
class PrintSpringProfile implements CommandLineRunner {

    @Autowired
    private Environment environment;

    @Value('${foo.message}')
    String message

    @Value('${pprop}')
    String pprop

    @Override
    public void run(String... args) throws Exception {

        println "info.app.name: ${environment.getProperty('info.app.name')}"
        println "Active profiles: ${environment.getActiveProfiles()}"
        println "Grails Env: ${grails.util.Environment.current}"
        println "foo.message: ${message}"
        println "testify.message: ${environment.getProperty('testify.message')}"
        println "pprop: ${pprop}"
    }
}
