package yakworks

import org.springframework.beans.factory.annotation.Autowired

import yakworks.openapi.gorm.OpenApiGenerator
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class OpenapiGeneratorSpec extends Specification {

    @Autowired
    OpenApiGenerator openApiGenerator

    def "sanity check generate"() {
        expect:
        openApiGenerator.generate()
        openApiGenerator.getApiSrcPath("api.yaml").exists()
        openApiGenerator.getApiBuildPath("api.yaml").exists()
    }

}
