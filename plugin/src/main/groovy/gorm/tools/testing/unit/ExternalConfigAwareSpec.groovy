package gorm.tools.testing.unit

import grails.boot.config.GrailsApplicationPostProcessor
import grails.testing.spock.OnceBefore
import org.springframework.beans.BeansException
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

import java.lang.reflect.Constructor

trait ExternalConfigAwareSpec {

    @OnceBefore
    void loadConfig() {
        defineBeans {
            externalConfigLoader(ExternalConfigLoader)
        }
    }
}

@Order(Ordered.HIGHEST_PRECEDENCE)
class ExternalConfigLoader implements ApplicationContextAware {

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        GrailsApplicationPostProcessor postProcessor = applicationContext.getBean('grailsApplicationPostProcessor')

        try {
            //run ExternalConfigRunListener it will add all the property sources to environment
            Constructor c = Class.forName("grails.plugin.externalconfig.ExternalConfigRunListener").getConstructor(SpringApplication, String[].class)
            Object listener = c.newInstance(null, null)
            listener.environmentPrepared(applicationContext.environment)
            postProcessor.loadApplicationConfig()
        }catch (ClassNotFoundException e) {
            //external config plugin is not installed
        }
    }
}

