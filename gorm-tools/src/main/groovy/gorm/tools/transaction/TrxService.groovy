/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.transaction

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus

import grails.gorm.transactions.GrailsTransactionTemplate
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional

/**
 * A generic way to wrap transaction with closures. Used in the WithTrx trait.
 */
@Transactional // this adds the getTransactionManager()
@CompileStatic
class TrxService {

    //dynamic wrapper so that static compile warnings dont show
    @CompileDynamic
    @NotTransactional
    PlatformTransactionManager getTrxManager() {
        return this.getTransactionManager() // <- this is added by the @Transactional
    }

    /**
     * Executes the given callable within the context of a transaction with the given definition
     *
     * @param callable The callable The callable
     * @return The result of the callable
     */
    @NotTransactional //marked as NotTransactional because it does it it self
    def <T> T withTrx(@ClosureParams(value = SimpleType,
                    options = "org.springframework.transaction.TransactionStatus") Closure<T> callable) {
        GrailsTransactionTemplate transTemplate = new GrailsTransactionTemplate(this.getTrxManager())
        transTemplate.execute { TransactionStatus transactionStatus ->
            return callable.call(transactionStatus)
        }
    }
}
