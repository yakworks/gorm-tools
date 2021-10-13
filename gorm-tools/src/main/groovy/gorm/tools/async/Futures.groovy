/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

import groovy.transform.CompileStatic

/**
 * Helpers for creating CompletableFuture and for running async
 */
@CompileStatic
class Futures {

    /**
     * run the closure asyncronously, if session is needed use asynService.runAsync
     * the runnable does not return a value but this returns a CompletableFuture
     * so you can call .join to block
     */
    static CompletableFuture<Void> runAsync(Runnable runnable){
        return CompletableFuture.runAsync(runnable)
    }

    /**
     * Creates a CompletableFuture passing supplier into supplyAsync
     * if asyncEnabled is false then it runs the supplyAsync.get synchronously and blocks, will return a completedFuture when done
     *
     * @param asyncEnabled if true then it runs a normal supplyAsync aschronously
     * @param supplier the supplier function or closure
     * @return the CompletableFuture
     */
    static <T> CompletableFuture<T> of(boolean asyncEnabled, Closure<T> supplierClosure){
        Supplier<T> supplier = supplierClosure as Supplier<T>
        return Futures.of(asyncEnabled, supplier)
    }

    /**
     * Creates a CompletableFuture passing supplier into supplyAsync
     * if asyncEnabled is false then it runs the supplyAsync.get synchronously and blocks, will return a completedFuture when done
     *
     * @param asyncEnabled if true then it runs a normal supplyAsync aschronously
     * @param supplier the supplier function or closure
     * @return the CompletableFuture
     */
    static <T> CompletableFuture<T> of(boolean asyncEnabled, Supplier<T> supplier){
        if(asyncEnabled){
            return CompletableFuture.supplyAsync(supplier)
        } else {
            CompletableFuture<T> syncFuture
            //fake it but run it in same thread
            try{
                T res = supplier.get()
                syncFuture = CompletableFuture.completedFuture(res)
            } catch (e){
                syncFuture = new CompletableFuture<T>()
                syncFuture.completeExceptionally(e)
            }
            return syncFuture
        }
    }

}
