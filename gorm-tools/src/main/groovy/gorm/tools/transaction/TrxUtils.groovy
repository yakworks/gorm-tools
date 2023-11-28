/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.transaction

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.core.Datastore
import org.grails.orm.hibernate.GrailsHibernateTemplate
import org.springframework.transaction.interceptor.TransactionAspectSupport
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * A generic way to wrap transaction with closures. Used in the WithTrx trait.
 *  - Work on same principal as @Transactional annotation.
 *  - Doesn't require a domain to use the closure
 *  - uses CustomizableRollbackTransactionAttribute which rollsback on any error
 *    where the domain.withTransaction uses DefaultTransactionDefinition
 *  - Also used as a central place to get the defualt Datastore
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.x
 */
@CompileStatic
class TrxUtils {

    /**
     * force a roll back if in a transaction
     * Can be used if wanting to rollback without firing an exception
     */
    static void rollback() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
    }

    /**
     * Calls flush if we are actively in a transaction, uses TransactionSynchronizationManager
     * if not in a transaction then flush is not valid concept anymore.
     * Everything is in a transaction if its db or datastore related
     * See the instance flush method if dont ahve datastore or are using the default one
     *
     * @param datastore the datastore for the flush
     */
    static void flush(Datastore datastore) {
        // only calls flush if we are actively in a trx
        if(TransactionSynchronizationManager.isSynchronizationActive()) {
            GrailsHibernateTemplate htemp = (GrailsHibernateTemplate)datastore.currentSession.nativeInterface
            //the flush method with object arg will run the flush inside of templates execute which will
            //transalate the exeptions to spring exceptions, the object arg does nothing
            htemp.flush("nothing")
        }
    }

    /**
     * Clears the cache for datastore if is has a current session
     * See the instance flush method if dont have datastore or are using the default one
     *
     * @param datastore the datastore for the flush
     */
    static void clear(Datastore ds) {
        if(ds.hasCurrentSession()) ds.currentSession.clear()
    }

    /**
     * flushes the session and clears the session cache and the DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
     */
    static void flushAndClear() {
        flush()
        clear()
    }

    /**
     * flushes the session
     */
    static void flush() {
        TrxService.bean().flush()
    }

    /**
     * clears the session cache
     */
    static void clear() {
        TrxService.bean().clear()
    }

}
