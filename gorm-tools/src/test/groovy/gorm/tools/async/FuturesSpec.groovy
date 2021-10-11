/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

import spock.lang.Specification

class FuturesSpec extends Specification {

    void "synchronous future example"() {
        when:
        String message

        def supplierFunc = { return 'foo' } as Supplier<String>

        Futures.of(false, supplierFunc).whenComplete{ String result, ex ->
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

        def supplierFunc = { throw new RuntimeException('some exception') } as Supplier<String>

        Futures.of(false, supplierFunc).whenComplete{ String result, ex ->
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

        def supplierFunc = { 'foo' } as Supplier<String>

        CompletableFuture completableFuture = Futures.of(true, supplierFunc).whenComplete{ String result, ex ->
            message = result
            println "CompletableFuture whenComplete with $result"
        }

        then:
        'foo' == completableFuture.join() //join waits for it to finish and returns val
        message == 'foo'

    }
}
