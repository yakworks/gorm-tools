/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.testing.spring.AutowiredTest
import spock.lang.Specification

class FuturesSpec extends GormToolsHibernateSpec implements AutowiredTest  {

    AsyncService asyncService

    void "synchronous future example"() {
        when:
        String message

        Supplier<String> supplierFunc = () -> { return 'foo' }

        asyncService.supplyAsync(new AsyncConfig(enabled:false), supplierFunc).whenComplete{ String result, ex ->
            assert result == 'foo'
            message = result
            println "whenComplete with $result"
        }

        then:
        message == 'foo'

    }

    void "synchronous future with exception"() {
        when:
        String message

        Supplier supplierFunc = () -> { throw new RuntimeException('some exception') }

        asyncService.supplyAsync(new AsyncConfig(enabled:false), supplierFunc).whenComplete{ String result, ex ->
            assert result == null
            assert ex.message == 'some exception'
            message = ex.message
            println "whenComplete with $ex"
        }

        then:
        message == 'some exception'

    }

    void "CompletableFuture"() {
        when:
        String message

        CompletableFuture completableFuture = asyncService.supplyAsync(() -> 'foo').whenComplete{ String result, ex ->
            message = result
            println "CompletableFuture whenComplete with $result"
        }

        then:
        'foo' == completableFuture.join() //join waits for it to finish and returns val
        message == 'foo'

    }
}
