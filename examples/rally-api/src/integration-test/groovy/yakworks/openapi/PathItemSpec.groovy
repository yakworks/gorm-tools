package yakworks.openapi

import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import yakworks.gorm.api.ApiConfig

@Integration
class PathItemSpec extends Specification {
    @Autowired ApiConfig apiConfig

    void "sanity check"() {
        expect:
        apiConfig
        apiConfig.paths
    }

    void "allowedOps are not explicitely specified"() {
        expect:
        !apiConfig.paths['/rally/activity'].allowedOps
        apiConfig.paths['/rally/activity'].upsertAllowed()
    }

    void "only update allowed"() {
        expect:
        apiConfig.paths['/rally/attachment'].allowedOps
        !apiConfig.paths['/rally/attachment'].upsertAllowed()
    }

    void "not allowed"() {
        expect:
        apiConfig.paths['/rally/partitionOrg'].allowedOps
        apiConfig.paths['/rally/partitionOrg'].allowedOps.size() == 1 && apiConfig.paths['/rally/partitionOrg'].allowedOps.contains('read')
        !apiConfig.paths['/rally/partitionOrg'].upsertAllowed()
    }
}
