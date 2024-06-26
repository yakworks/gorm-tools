/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest

import groovy.transform.CompileStatic

import org.apache.catalina.connector.Connector
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer

/**
 * Spring Customizer that makes it so tomcat doesnt error for special chars such as |,{},and []. The rest api uses them to pass json as params
 * This makes it much easier if its not required to escape them
 */
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
