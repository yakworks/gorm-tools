package gorm.tools.rest

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer

class RestTomcatWebServerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    @Override
    void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers({
            //it.setProperty('relaxedPathChars', '<>[\\]^`{|}')
            it.setProperty('relaxedQueryChars', '|{}[]')
        } as TomcatConnectorCustomizer)
    }
}
