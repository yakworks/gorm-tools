package yakworks.openapi

import yakworks.openapi.gorm.OpenApiGenerator
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.rally.api.SpringApplication

@Integration(applicationClass = SpringApplication)
@Rollback
class OpenapiGeneratorSpec extends Specification {

    @Autowired
    OpenApiGenerator openApiGenerator

    def "sanity check generate"() {
        expect:
        openApiGenerator.getApiSrcPath("api.yaml").exists()
        openApiGenerator.generate()
        openApiGenerator.getApiBuildPath("api.yaml").exists()
    }

}
