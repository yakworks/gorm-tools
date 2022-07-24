package yakworks.openapi

import gorm.tools.openapi.OpenApiGenerator
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class OpenapiGeneratorSpec extends Specification {

    @Autowired
    OpenApiGenerator openApiGenerator

    def "sanity check generate"() {
        expect:
        openApiGenerator.generate()
        openApiGenerator.getApiSrcPath("api.yaml").toFile().exists()
        openApiGenerator.getApiBuildPath("api.yaml").toFile().exists()
    }

}
