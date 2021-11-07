package gorm.tools.transaction


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Rollback
@Integration
class TrxServiceRollbackSpec extends Specification {

    TrxService trxService

    void "transaction status is setup"() {
        when:
        def stat = trxService.withTrx { trxStatus ->
            trxStatus.hasTransaction()
            trxStatus.newTransaction
            !trxStatus.readOnly
            true
        }

        then:
        stat
        trxService.transactionManager
        trxService.targetDatastore
    }

}
