/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.rest

import groovy.transform.CompileStatic

import org.apache.catalina.connector.Connector
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer

@CompileStatic
class RestTomcatWebServerCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    @Override
    void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers({Connector connector ->
            //it.setProperty('relaxedPathChars', '<>[\\]^`{|}')
            connector.setProperty('relaxedQueryChars', '|{}[]')
        } as TomcatConnectorCustomizer)
    }
}
