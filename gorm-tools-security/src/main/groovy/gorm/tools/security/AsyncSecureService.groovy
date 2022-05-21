/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security


import java.util.function.Consumer
import java.util.function.Supplier

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.core.Datastore
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

import gorm.tools.async.AsyncService

/**
 * AsyncService that copies the authentication to context while in the new thread
 * see https://github.com/spring-projects/spring-security/issues/6856
 * MODE_INHERITABLETHREADLOCAL does not work with our CompletbaleFutures,
 * It also makes it insecure as the prior user is used during auth check during rest
 */
@Slf4j
@CompileStatic
class AsyncSecureService extends AsyncService  {

    static Authentication authentication(){
        SecurityContextHolder.context.authentication
    }

    static void setAuth(Authentication auth){
        SecurityContextHolder.context.authentication = auth
    }

    @Override
    public <T> Supplier<T> wrapSupplier(Supplier<T> sup) {
        def authentication = authentication()
        return new Supplier<T>() {
            @Override
            T get() {
                setAuth(authentication)
                return sup.get()
            }
        }
    }

    @Override
    public <T> Supplier<T> wrapSupplierTrx(Datastore ds, Supplier<T> sup) {
        wrapSupplier(super.wrapSupplierTrx(ds, sup))
    }

    @Override
    public <T> Supplier<T> wrapSupplierSession(Datastore ds, Supplier<T> sup) {
        wrapSupplier(super.wrapSupplierSession(ds, sup))
    }

    /**
     * wrap the consumer, can be overriden in super which is is done in AsyncSecureService
     */
    @Override
    public <T> Consumer<T> wrapConsumer(Consumer<T> consumer) {
        def authentication = authentication()
        return new Consumer<T>() {
            @Override
            void accept(T item) {
                setAuth(authentication)
                consumer.accept(item)
            }
        }
    }

    @Override
    public <T> Consumer<T> wrapConsumerTrx(Datastore ds, Consumer<T> consumer) {
        wrapConsumer(super.wrapConsumerTrx(ds, consumer))
    }

    @Override
    public <T> Consumer<T> wrapConsumerSession(Datastore ds, Consumer<T> consumer) {
        wrapConsumer(super.wrapConsumerSession(ds, consumer))
    }

}
