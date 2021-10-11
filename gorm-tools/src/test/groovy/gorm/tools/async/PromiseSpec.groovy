/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.text.SimpleDateFormat

import io.activej.common.function.BiFunctionEx
import io.activej.common.function.ConsumerEx
import io.activej.common.function.FunctionEx
import io.activej.eventloop.Eventloop
import io.activej.promise.Promise
import io.activej.promise.Promises
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import yakworks.commons.lang.DateUtil
import yakworks.commons.lang.IsoDateUtil

class PromiseSpec extends Specification {
    private static final Eventloop eventloop = Eventloop.create().withCurrentThread();

    // void setupSpec(){
    //     tester.setTimeZone(TimeZone.getTimeZone('UTC'))
    // }

    void "promise example"() {
        expect:
        doSomeProcess()
            .whenResult({result -> printf("Result of some process is '%s'%n", result)} as ConsumerEx)
            .whenException( {e -> System.out.printf("Exception after some process is '%s'%n", e.getMessage())} as ConsumerEx)
            .map({s -> s.toLowerCase()} as FunctionEx)
            .map({ result, e -> e == null ? String.format("The mapped result is '%s'", result) : e.getMessage()} as BiFunctionEx)
            .whenResult({s -> System.out.println(s)} as ConsumerEx);

        eventloop.run();

    }

    public static Promise<String> doSomeProcess() {
        return Promises.delay(1000, Math.random() > 0.5 ?
            Promise.of("Hello World") :
            Promise.ofException(new RuntimeException("Something went wrong")));
    }

}
