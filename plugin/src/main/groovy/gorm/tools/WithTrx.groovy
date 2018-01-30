package gorm.tools

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionStatus

import javax.annotation.PostConstruct

/**
 * adds transaction methods to any class. relies on Gorms transactionService.
 */
@CompileStatic
trait WithTrx {

    @Autowired
    TrxService trxService

    @PostConstruct
    void init() {
        //fixes "Field gorm_tools_WithTrx__transactionService in gorm.tools.async.GparsBatchSupport required a bean of type 'grails.gorm.transactions.TransactionService' that could not be found.
        //transactionService = AppCtx.get("transactionService",TransactionService)
    }
    /**
     * Executes the given callable within the context of a transaction with the given definition
     *
     * @param definition The transaction definition as a map
     * @param callable The callable The callable
     * @return The result of the callable
     */
//    public <T> T withTrx(Map definition, @ClosureParams(value = SimpleType.class,
//        options = "org.springframework.transaction.TransactionStatus") Closure<T> callable) {
//        if(!transactionService) transactionService = AppCtx.get("transactionService",TransactionService)
//        transactionService.withTransaction(definition, callable)
//    }

    /**
     * Executes the given callable within the context of a transaction with the given definition
     *
     * @param callable The callable The callable
     * @return The result of the callable
     */
    public <T> T withTrx(@ClosureParams(value = SimpleType.class,
        options = "org.springframework.transaction.TransactionStatus") Closure<T> callable) {
        trxService.withTrx(callable)
    }

    void flushAndClear(TransactionStatus status) {
        status.flush()
        clear(status)
    }

    @CompileDynamic
    void clear(TransactionStatus status) {
        //TransactionObject txObject = (status as DefaultTransactionStatus).transaction as TransactionObject
        status.transaction.sessionHolder.getSession().clear()
    }

}
