/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Supplier
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.core.Datastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.TransactionStatus

import gorm.tools.support.ConfigAware
import gorm.tools.transaction.TrxService
import grails.persistence.support.PersistenceContextInterceptor

/**
 * Support service for aysnc to wrap session, transaction, etc...
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.9
 */
@Slf4j
@CompileStatic
class AsyncService implements ConfigAware  {

    /**
     * The default slice or chunk size for collating. for example if this is 100 and you pass list of of 100
     * then it will slice it or collate it into a list with 10 of lists with 100 items each.
     * should default to the hibernate.jdbc.batch_size in the implementation. Usually best to set this around 100
     */
    @Value('${gorm.tools.async.sliceSize:100}')
    int sliceSize

    /** The list size to send to the collate that slices.*/
    @Value('${gorm.tools.async.enabled:true}')
    boolean asyncEnabled

    /** the pool size, defaults to 4 right now, parralel gets it own their own */
    @Value('${gorm.tools.async.poolSize:4}')
    int poolSize

    @Autowired
    PersistenceContextInterceptor persistenceInterceptor

    @Autowired
    TrxService trxService

    /** setup defaults for poolSize and batchSize if config isn't present. batchSize set to 100 if not config found*/
    @PostConstruct
    void init() {
        //if batchSize is 0 then hibernate may not bbe installed and hibernate.jdbc.batch_size is not set. force it to 100
        Integer batchSize = config.getProperty('hibernate.jdbc.batch_size', Integer)
        sliceSize = batchSize ?: sliceSize
    }

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
    CompletableFuture<Void> runAsync(AsyncConfig asyncConfig, Closure runnable){
        Closure wrappedClosure = wrapClosure(asyncConfig, false, runnable)
        if(shouldAsync(asyncConfig)){
            return CompletableFuture.runAsync(wrappedClosure)
        } else {
            //run it now and return a dummy CompletableFuture
            wrappedClosure.call()
            return CompletableFuture.completedFuture(null) as CompletableFuture<Void>
        }
    }

    /**
     * shortcut that proved a default AsyncConfig
     */
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier){
        supplyAsync(new AsyncConfig(), supplier)
    }

    /**
     * Supplies a CompletableFuture and returns it, can be wrapped in session or trx if specified in asyncConfig
     *
     * @param asyncConfig the config object that can have session or transactional set if it should be wrapped
     * @param closure the runnable closure
     * @return the CompletableFuture
     */
    public <T> CompletableFuture<T> supplyAsync(AsyncConfig asyncConfig, Supplier<T> supplier){
        Supplier<T> wrappedSupplier = wrapSupplier(asyncConfig, supplier)
        if(shouldAsync(asyncConfig)){
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

    boolean shouldAsync(AsyncConfig asyncConfig){
        return asyncConfig.enabled != null ? asyncConfig.enabled : getAsyncEnabled()
    }

    /**
     * calls wrapSessionOrTransaction setting passItem to true
     */
    Closure wrapClosure(AsyncConfig asyncArgs, Closure closure){
        wrapClosure(asyncArgs, true, closure)
    }
    /**
     * checks args for session or trx and wraps the closure if needed
     */
    /**
     * checks args for session or trx and wraps the closure if needed
     *
     * @param asyncArgs the args that decide if its a trx or session
     * @param passItem it true then resulting closure will accept an item arg so it can be used to pass to an each
     * @param closure the closure to wrap
     * @return the wrapped closure
     */
    Closure wrapClosure(AsyncConfig asyncArgs, boolean passItem, Closure closure){
        Closure wrappedClosure = closure
        if(asyncArgs.transactional){
            verifyDatastore(asyncArgs)
            wrappedClosure = passItem ? wrapTrx(asyncArgs.datastore, closure) : wrapClosureTrx(asyncArgs.datastore, closure)
        } else if(asyncArgs.session){
            verifyDatastore(asyncArgs)
            wrappedClosure = passItem ? wrapSession(asyncArgs.datastore, closure) : wrapClosureSession(asyncArgs.datastore, closure)
        }
        wrappedClosure
    }

    /**
     * checks args for session or trx and wraps the closure if needed
     *
     * @param asyncArgs the args that decide if its a trx or session
     * @param passItem it true then resulting closure will accept an item arg so it can be used to pass to an each
     * @param closure the closure to wrap
     * @return the wrapped supplier
     */
    public <T> Supplier<T> wrapSupplier(AsyncConfig asyncArgs, Supplier<T> supplier){
        Supplier<T> wrappedSupplier = supplier
        if(asyncArgs.transactional){
            verifyDatastore(asyncArgs)
            wrappedSupplier = wrapSupplierTrx(asyncArgs.datastore, supplier)
        } else if(asyncArgs.session){
            verifyDatastore(asyncArgs)
            wrappedSupplier = wrapSupplierSession(asyncArgs.datastore, supplier)
        }
        wrappedSupplier
    }

    /**
     * if args doesn't have a datastore then it grabs the default one from the trxService
     */
    void verifyDatastore(AsyncConfig asyncArgs){
        if(!asyncArgs.datastore){
            asyncArgs.datastore = trxService.getTargetDatastore()
        }
    }

    /**
     * Wrap closure in a transaction for an item, use for and each iterator
     */
    public <T> Closure<T> wrapTrx(Datastore ds, Closure<T> c) {
        return { T item ->
            trxService.withTrx(ds) { TransactionStatus status ->
                c.call(item)
            }
        }
    }

    Closure wrapClosureTrx(Datastore ds, Closure c) {
        return { ->
            trxService.withTrx(ds) { TransactionStatus status ->
                c.call(status)
            }
        }
    }

    /**
     * wrap closure in a session
     */
    @SuppressWarnings(["EmptyCatchBlock"])
    Closure wrapClosureSession(Datastore ds, Closure wrapped) {
        return { ->
            persistenceInterceptor.init()
            try {
                wrapped.call()
            } finally {
                try {
                    //only destroys if new one was created, otherwise does nothing
                    persistenceInterceptor.destroy()
                } catch (Exception e) {
                    //ignore errors
                }
            }
        }
    }

    /**
     * wrap closure in a session
     */
    @SuppressWarnings(["EmptyCatchBlock"])
    public <T> Closure<T> wrapSession(Datastore ds, Closure<T> wrapped) {
        return { T item ->
            persistenceInterceptor.init()
            try {
                wrapped.call(item)
            } finally {
                try {
                    //only destroys if new one was created, otherwise does nothing
                    persistenceInterceptor.destroy()
                } catch (Exception e) {
                    //ignore errors
                }
            }
        }
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
        return new Supplier<T>() {
            @Override
            T get() {
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
        }
    }


    public <T> Consumer<T> wrapConsumer(AsyncConfig asyncArgs, Consumer<T> consumer){
        Consumer<T> wrappedConsumer

        if(asyncArgs.transactional){
            verifyDatastore(asyncArgs)
            wrappedConsumer = wrapConsumerTrx(asyncArgs.datastore, consumer)
        } else if(asyncArgs.session){
            verifyDatastore(asyncArgs)
            wrappedConsumer = wrapConsumerSession(asyncArgs.datastore, consumer)
        } else {
            // no wrap so just do closure
            wrappedConsumer = consumer
        }
        wrappedConsumer
    }

    public <T> Consumer<T> wrapConsumerTrx(Datastore ds, Consumer<T> consumer) {
        return new Consumer<T>() {
            @Override
            void accept(T item) {
                trxService.withTrx(ds) { TransactionStatus status ->
                    consumer.accept(item)
                }
            }
        }
    }

    @SuppressWarnings(["EmptyCatchBlock"])
    public <T> Consumer<T> wrapConsumerSession(Datastore ds, Consumer<T> consumer) {
        return new Consumer<T>() {
            @Override
            void accept(T item) {
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
        }
    }

}
