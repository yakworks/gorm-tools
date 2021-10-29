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
import org.grails.datastore.mapping.core.Session
import org.grails.datastore.mapping.transactions.CustomizableRollbackTransactionAttribute
import org.grails.datastore.mapping.transactions.TransactionCapableDatastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus

import gorm.tools.repository.RepoUtil
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

    Datastore getTargetDatastore() {
        return this.targetDatastore != null ? this.targetDatastore : GormEnhancer.findSingleDatastore()
    }

    Datastore getDatastore(Class entityClass) {
        def repo = RepoUtil.findRepo(entityClass)
        return repo != null? repo.datastore : getTargetDatastore()
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
     * Executes the closure within the context of a transaction for the given Datastore
     * Will use the tr
     *
     * @param callable The closure to call
     * @return The result of the closure execution
     */
    public <T> T withTrx(Datastore datastore,
                         @ClosureParams(value = SimpleType, options = "org.springframework.transaction.support.DefaultTransactionStatus") Closure<T> callable) {
        if (!callable) {
            return
        }
        PlatformTransactionManager trxManager
        //if datastore is set then use its transactionManager, useful in multi datasource situations
        if(datastore && datastore instanceof TransactionCapableDatastore){
            trxManager = datastore.transactionManager
        }
        def tranDef = new CustomizableRollbackTransactionAttribute()
        new GrailsTransactionTemplate(trxManager, tranDef).execute(callable)
    }

    public <T> T withSession(Closure<T> callable){
        getTargetDatastore().withSession callable
    }

    Session getCurrentSession(){
        getTargetDatastore().currentSession
    }

    void flushAndClear(TransactionStatus status) {
        status.flush()
        def ses = getCurrentSession()
        ses.clear()
    }

    // @CompileDynamic
    // void clear(TransactionStatus status) {
    //     // FIXME The reason this is compile dynamic might just be how unit tests wire things up for transactions
    //     // might just need to move tests over to integration and see.
    //     //Cannot cast object 'org.springframework.orm.hibernate5.HibernateTransactionManager$HibernateTransactionObject
    //     // def defTrxStatus = (DefaultTransactionStatus)status
    //     // // TransactionObject txObject = (TransactionObject) defTrxStatus.getTransaction()
    //     // final SessionHolder sessionHolder = defTrxStatus['sessionHolder'](SessionHolder)
    //     // sessionHolder.getSession().clear()
    //     status.transaction.sessionHolder.getSession().clear()
    // }
}