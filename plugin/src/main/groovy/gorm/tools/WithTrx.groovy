package gorm.tools

import gorm.tools.TrxService
import groovy.transform.CompileStatic
import groovy.transform.SelfType
import org.springframework.beans.factory.annotation.Autowired

@CompileStatic
trait WithTrx {

    @Autowired
    TrxService trxService

    public <T> T withTrx(Map transProps = [:], Closure<T> callable) {
        //GormEnhancer.findStaticApi(getDomainClass()).withTransaction(transProps, callable)
        //this seems to be faster than the withTransaction on the static gorm api. the TrxService seems to be as fast
        //TransactionService txService = getDatastore().getService(TransactionService)
        //txService.withTransaction(transProps, callable)
        trxService.withTrx(callable)
        //callable()
    }

}
