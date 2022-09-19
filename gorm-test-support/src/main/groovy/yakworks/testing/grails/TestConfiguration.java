package yakworks.testing.grails;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

// @Configuration(proxyBeanMethods = false)
@Configuration
// @EnableConfigurationProperties //({AsyncConfig.class, GormConfig.class})
@ConfigurationPropertiesScan
public class TestConfiguration {

}
