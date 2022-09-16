package yakworks.testing.grails;

import gorm.tools.settings.AsyncProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// @Configuration(proxyBeanMethods = false)
@Configuration
@EnableConfigurationProperties(AsyncProperties.class)
public
class BasicConfiguration {

}
