package yakworks.spring.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix="job.test")
class CronJobPropsImpl extends CronJobProps {
}
