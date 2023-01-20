/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Supplier

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.core.Datastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionStatus

import gorm.tools.job.SyncJobContext
import gorm.tools.problem.ProblemHandler
import gorm.tools.transaction.TrxService
import grails.persistence.support.PersistenceContextInterceptor
import yakworks.gorm.config.AsyncConfig

/**
 * Support service for aysnc to wrap session, transaction, etc...
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.9
 */
@Slf4j
@CompileStatic
class AsyncService {

    @Autowired AsyncConfig asyncConfig

    @Autowired
    PersistenceContextInterceptor persistenceInterceptor

    @Autowired
    TrxService trxService

    @Autowired
    ProblemHandler problemHandler
    // static cheater to get the bean, use sparingly if at all
    // static AsyncService getBean(){
    //     AppCtx.get('asyncService', this)
    // }

    /**
     * run the closure asyncronously,
     *
     * @param asyncConfig the config object that can have session or transactional set if it should be wrapped
     * @param runnable the runnable closure
     * @return the CompletableFuture
     */
    CompletableFuture<Void> runAsync(AsyncArgs asyncArgs, Runnable runnable){
        return supplyAsync( asyncArgs, (Supplier<Void>)( () -> runnable.run() ))
    }

    /**
     * shortcut that proved a default AsyncConfig
     */
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier){
        supplyAsync(new AsyncArgs(), supplier)
    }

    /**
     * Supplies a CompletableFuture and returns it, can be wrapped in session or trx if specified in asyncConfig
     *
     * @param asynArgs the config object that can have session or transactional set if it should be wrapped
     * @param closure the runnable closure
     * @return the CompletableFuture
     */
    public <T> CompletableFuture<T> supplyAsync(AsyncArgs asynArgs, Supplier<T> supplier){
        Supplier<T> wrappedSupplier = wrapSupplier(asynArgs, supplier)
        if(shouldAsync(asynArgs)){
            return CompletableFuture.supplyAsync(wrappedSupplier)
        } else {
            CompletableFuture<T> syncFuture
            //fake it but run it in same thread
            try{
                T res = wrappedSupplier.get()
                syncFuture = CompletableFuture.completedFuture(res)
            } catch (e){
                syncFuture = new CompletableFuture<T>()
                syncFuture.completeExceptionally(e)
            }
            return syncFuture
        }
    }

    boolean shouldAsync(AsyncArgs asyncArgs){
        return asyncArgs.enabled != null ? asyncArgs.enabled : asyncConfig.enabled
    }

    /**
     * if args doesn't have a datastore then it grabs the default one from the trxService
     */
    void verifyDatastore(AsyncArgs asyncArgs){
        if(!asyncArgs.datastore){
            asyncArgs.datastore = trxService.getTargetDatastore()
        }
    }

    /**
     * checks args for session or trx and wraps the closure if needed
     *
     * @param asyncArgs the args that decide if its a trx or session
     * @param passItem it true then resulting closure will accept an item arg so it can be used to pass to an each
     * @param closure the closure to wrap
     * @return the wrapped supplier
     */
    public <T> Supplier<T> wrapSupplier(AsyncArgs asyncArgs, Supplier<T> supplier){
        Supplier<T> wrappedSupplier
        if(asyncArgs.transactional){
            verifyDatastore(asyncArgs)
            wrappedSupplier = wrapSupplierTrx(asyncArgs.datastore, supplier)
        } else if(asyncArgs.session){
            verifyDatastore(asyncArgs)
            wrappedSupplier = wrapSupplierSession(asyncArgs.datastore, supplier)
        } else {
            wrappedSupplier = wrapSupplier(supplier)
        }
        wrappedSupplier
    }

    /**
     * just returns the passed in supplier by default, an be overriden in a super
     * which is done for the AsyncSecurityService
     */
    public <T> Supplier<T> wrapSupplier(Supplier<T> sup) {
        return sup
    }

    public <T> Supplier<T> wrapSupplierTrx(Datastore ds, Supplier<T> sup) {
        return new Supplier<T>() {
            @Override
            T get() {
                trxService.withTrx(ds) { TransactionStatus status ->
                    return sup.get()
                }
            }
        }
    }

    @SuppressWarnings(["EmptyCatchBlock"])
    public <T> Supplier<T> wrapSupplierSession(Datastore ds, Supplier<T> sup) {
        Supplier<T> newsup = () -> {
            persistenceInterceptor.init()
            try {
                return sup.get()
            } finally {
                try {
                    //only destroys if new one was created, otherwise does nothing
                    persistenceInterceptor.destroy()
                } catch (Exception e) {
                    //ignore errors
                }
            }
        }
        return newsup
    }


    public <T> Consumer<T> wrapConsumer(AsyncArgs asyncArgs, Consumer<T> consumer){
        Consumer<T> wrappedConsumer

        if(asyncArgs.transactional){
            verifyDatastore(asyncArgs)
            wrappedConsumer = wrapConsumerTrx(asyncArgs.datastore, consumer)
        } else if(asyncArgs.session){
            verifyDatastore(asyncArgs)
            wrappedConsumer = wrapConsumerSession(asyncArgs.datastore, consumer)
        } else {
            // no wrap so just do closure
            wrappedConsumer = wrapConsumer(consumer)
        }
        wrappedConsumer
    }

    /**
     * wrap the consumer, can be overriden in super which is is done in AsyncSecureService to copy security context into new thread
     */
    public <T> Consumer<T> wrapConsumer(Consumer<T> consumer) {
        return consumer
    }

    public <T> Consumer<T> wrapConsumerTrx(Datastore ds, Consumer<T> consumer) {
        Consumer<T> newcon = (T item) -> {
            trxService.withTrx(ds) { TransactionStatus status ->
                consumer.accept(item)
            }
        }
        return newcon
    }

    @SuppressWarnings(["EmptyCatchBlock"])
    public <T> Consumer<T> wrapConsumerSession(Datastore ds, Consumer<T> consumer) {
        Consumer<T> newcon = (T item) -> {
            persistenceInterceptor.init()
            try {
                consumer.accept(item)
            } finally {
                try {
                    //only destroys if new one was created, otherwise does nothing
                    persistenceInterceptor.destroy()
                } catch (Exception e) {
                    //ignore errors
                }
            }
        }
        return newcon
    }

}
