/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import grails.gorm.transactions.Transactional

@Transactional
@CompileStatic
class TrxService {

    /**
     * Executes the given callable within the context of a transaction with the given definition
     *
     * @param callable The callable The callable
     * @return The result of the callable
     */
    def <T> T withTrx(@ClosureParams(value = SimpleType,
        options = "org.springframework.transaction.TransactionStatus") Closure<T> callable) {
        callable.call(transactionStatus)
    }
}
