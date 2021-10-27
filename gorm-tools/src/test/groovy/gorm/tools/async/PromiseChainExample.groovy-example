package gorm.tools.async

import io.activej.common.function.BiFunctionEx
import io.activej.common.function.ConsumerEx
import io.activej.common.function.FunctionEx
import io.activej.eventloop.Eventloop
import io.activej.promise.Promise
import io.activej.promise.Promises

class PromiseChainExample {
    private static final Eventloop eventloop = Eventloop.create().withCurrentThread();

    static void main(String[] args) {
        doSomeProcess()
            .whenResult({result -> printf("Result of some process is '%s'%n", result)} as ConsumerEx)
            .whenException( {e -> System.out.printf("Exception after some process is '%s'%n", e.getMessage())} as ConsumerEx)
            .map({s -> s.toLowerCase()} as FunctionEx)
            .map({ result, e -> e == null ? String.format("The mapped result is '%s'", result) : e.getMessage()} as BiFunctionEx)
            .whenResult({s -> System.out.println(s)} as ConsumerEx);

        // eventloop.run();
    }

    static Promise<String> loadData() {
        return Promise.of("Hello World");
    }

    static Promise<String> doSomeProcess() {
        return Promises.delay(1000, Math.random() > 0.5 ?
            Promise.of("Hello World") :
            Promise.ofException(new RuntimeException("Something went wrong")));
    }
}
