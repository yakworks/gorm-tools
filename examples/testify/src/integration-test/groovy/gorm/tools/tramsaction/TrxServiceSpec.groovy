package gorm.tools.tramsaction

import gorm.tools.transaction.TrxService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
class TrxServiceSpec extends Specification {

    TrxService trxService

    void "transaction status is setup correctly"() {
        when:
        def stat = trxService.withTrx { trxStatus ->
            trxStatus.hasTransaction()
            trxStatus.newTransaction
            !trxStatus.readOnly
            !trxStatus.isRollbackOnly()
            trxStatus
        }

        then:
        stat.completed
        trxService.transactionManager
        trxService.targetDatastore
    }

}
