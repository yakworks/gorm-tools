package yakworks.openapi

import spock.lang.Ignore
import yakworks.openapi.gorm.OpenApiGenerator
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification


@Integration
@Rollback
//XXX @SUD see if you can see why this is breaking the binding
//if we enable this test then it seems to do something to the dataBytes in SyncJob that has display:false set
// if this test runs first, then the BulkRestApiSpec fails since the SyncJobService no longer updates the dataBytes. Its weird.
@Ignore
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
