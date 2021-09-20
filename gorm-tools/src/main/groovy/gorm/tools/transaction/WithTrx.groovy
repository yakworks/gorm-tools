/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.transaction

import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import org.springframework.transaction.TransactionStatus

/**
 * adds transaction methods to any class. relies on Gorms transactionService.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait WithTrx {

    @Inject
    TrxService trxService

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
    public <T> T withTrx(@ClosureParams(value = SimpleType,
                      options = "org.springframework.transaction.TransactionStatus") Closure<T> callable) {
        getTrxService().withTrx(callable)
    }

    public <T> T withSession(Closure<T> callable){
        getTrxService().withSession callable
    }

    void flushAndClear(TransactionStatus status) {
        getTrxService().flushAndClear(status)
    }


    // @CompileDynamic
    // void clear(TransactionStatus status) {
    //     trxService.clear(status)
    // }

}
