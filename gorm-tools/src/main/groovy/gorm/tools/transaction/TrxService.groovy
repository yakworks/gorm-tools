/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.transaction


import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.internal.RuntimeSupport
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.transactions.CustomizableRollbackTransactionAttribute
import org.grails.datastore.mapping.transactions.TransactionCapableDatastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition

import grails.gorm.transactions.GrailsTransactionTemplate

/**
 * A generic way to wrap transaction with closures. Used in the WithTrx trait.
 * Work on same principal as @Transactional annotation.
 * Doesn't require a domain to use the closure
 * and uses CustomizableRollbackTransactionAttribute which rollsback on any error
 * where the domain.withTransaction uses DefaultTransactionDefinition
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.x
 */
@CompileStatic
class TrxService {

    PlatformTransactionManager transactionManager
    Datastore targetDatastore

    PlatformTransactionManager getTransactionManager() {
        return this.transactionManager != null ? this.transactionManager : GormEnhancer.findSingleTransactionManager()
    }

    @Autowired(required = false)
    void setTargetDatastore(Datastore... datastores) {
        Datastore defds = RuntimeSupport.findDefaultDatastore(datastores)
        this.targetDatastore = defds
        if (defds != null) {
            this.transactionManager = ((TransactionCapableDatastore) defds).transactionManager
        }
    }

    protected Datastore getTargetDatastore() {
        return this.targetDatastore != null ? this.targetDatastore : GormEnhancer.findSingleDatastore()
    }

    /**
     * Executes the given callable within the context of a transaction with the given definition
     *
     * @param callable The callable The callable
     * @return The result of the callable
     */
    public <T> T withTrx(@ClosureParams(value = SimpleType, options = "org.springframework.transaction.support.DefaultTransactionStatus") Closure<T> callable) {

        def tranDef = new CustomizableRollbackTransactionAttribute()
        withTrx(tranDef, callable)

    }

    /**
     * Executes the closure within the context of a transaction for the given {@link org.springframework.transaction.TransactionDefinition}
     *
     * @param callable The closure to call
     * @return The result of the closure execution
     */
    public <T> T withTrx(Map transactionProperties,
                         @ClosureParams(value = SimpleType, options = "org.springframework.transaction.support.DefaultTransactionStatus") Closure<T> callable) {

        def tranDef = new CustomizableRollbackTransactionAttribute()
        transactionProperties.each { k, v ->
            try {
                tranDef[k as String] = v
            } catch (MissingPropertyException mpe) {
                throw new IllegalArgumentException("[${k}] is not a valid transaction property.")
            }
        }

        withTrx(tranDef, callable)
    }

    /**
     * Executes the closure within the context of a transaction for the given {@link org.springframework.transaction.TransactionDefinition}
     *
     * @param callable The closure to call
     * @return The result of the closure execution
     */
    public <T> T withTrx(TransactionDefinition definition,
                         @ClosureParams(value = SimpleType, options = "org.springframework.transaction.support.DefaultTransactionStatus") Closure<T> callable) {

        if (!callable) {
            return
        }

        new GrailsTransactionTemplate(getTransactionManager(), definition).execute(callable)
    }

    /**
     * Conditionally Executes the closure within the context of a transaction
     * Make its easier to reason about in flow when the transactional wrapper is optional
     *
     * @param isTransactional whether its transactional or not
     * @param callable The closure to call
     * @return The result of the closure execution
     */
    public <T> T withOptionalTrx(boolean isTransactional,
                         @ClosureParams(value = SimpleType, options = "org.springframework.transaction.support.DefaultTransactionStatus") Closure<T> callable) {

        if (!callable) {
            return
        }
        if(isTransactional){
            withTrx(callable)
        } else{
            return callable.call(null)
        }
    }

    public <T> T withSession(Closure<T> callable){
        getTargetDatastore().withSession callable
    }
}
