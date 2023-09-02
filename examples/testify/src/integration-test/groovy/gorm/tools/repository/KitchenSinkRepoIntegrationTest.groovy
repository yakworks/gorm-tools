package gorm.tools.repository

import gorm.tools.transaction.TrxService
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification
import yakworks.api.problem.data.DataProblemCodes
import yakworks.api.problem.data.DataProblemException
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.testing.gorm.model.KitchenSink

@Integration
@Rollback
class KitchenSinkRepoIntegrationTest extends Specification implements DomainIntTest {

    TrxService trxService
    JdbcTemplate jdbcTemplate

    //not transactional, so that the "remove" runs in own transaction, just how it would run when called from controller
    @NotTransactional
    void "delete violates fk constraint"() {
        setup:
        KitchenSink sink
        KitchenSink sink2
        trxService.withNewTrx {
            jdbcTemplate.execute("ALTER TABLE KitchenSink ADD constraint fk_link FOREIGN KEY (sinkLinkId) REFERENCES KitchenSink(id)")
            sink = KitchenSink.create(num: "K1", name: "K1")
            sink2 = KitchenSink.create(num: "K2", name: "K2")
            sink2.sinkLink = sink
            sink2.persist(flush: true)
        }

        when:
        KitchenSink.repo.removeById(sink.id)

        then:
        //GormRepo catches fk violations exceptions and uses RepoExceptionSupport, which converts it to DataProblemException
        DataProblemException ex = thrown()
        ex.code == DataProblemCodes.ReferenceKey.code
        ex.message.contains('FK_LINK')
        ex.message.contains('Referential integrity constraint violation')

        cleanup:
        trxService.withNewTrx {
            jdbcTemplate.execute("ALTER TABLE KitchenSink drop constraint fk_link")
            sink2.delete()
            sink.delete()
        }
    }

    //not transactional, to verify that "exists" wraps query in trx
    @NotTransactional
    void "test exists"() {
        expect:
        KitchenSink.repo.exists(1L)
        !KitchenSink.repo.exists(999999)
    }
}
