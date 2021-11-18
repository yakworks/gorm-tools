package yakworks.rally.orgs

import gorm.tools.api.DataAccessProblem
import gorm.tools.api.EntityValidationProblem
import gorm.tools.api.UniqueConstraintProblem
import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.repo.OrgSourceRepo

@Integration
@Rollback
class OrgSourceRepoTests extends Specification implements DataIntegrationTest {

    OrgSourceRepo orgSourceRepo

    void testSaveFail_WithDuplicateSourceId() {
        when:

        Map createOrg = [sourceType:'App', sourceId:'99', orgId:99, orgType: [id: 1]]
        orgSourceRepo.create(createOrg, [flush:true])
        //orgSourceRepo.flushAndClear()

        then:
        UniqueConstraintProblem ge = thrown()
        ge.detail.contains("Unique index or primary key violation") || //mysql and H2
                ge.detail.contains("Duplicate entry") || //mysql
                ge.detail.contains("Violation of UNIQUE KEY constraint") || //sql server
                ge.detail.contains("duplicate key value violates unique constraint") //postgres
    }

    void "testSave success same sourceId on different orgTypes"() {
        when:
        Map createOrg = [source:'9ci', sourceType:'ERP', sourceId:'K14700', orgId:124, orgType: [id: 2]]
        OrgSource source = orgSourceRepo.create(createOrg, [flush:true])

        then: "should pass because new OrgSource is for different orgType"
        source
        'K14700' == source.sourceId

    }
}
