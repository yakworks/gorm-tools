package gorm.tools.async

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

import groovy.transform.CompileStatic

@CompileStatic
class CompletableFutureExample {

    static void main(String[] args) {

        example1()

        // .supplyAsync( -> this.&doSomeProcess)
        // .thenApply{ res -> println "res is $res" }



        // String val = supplyAsync
        //     .handle{res, ex ->
        //         println "handle res is $res"
        //         println "handle ex is $ex"
        //     }
        //     .join()
            // .whenComplete{ res, ex ->
            //     println "whenComplete res is $res"
            //     println "whenComplete ex is $ex"
            // }
        // println "val is $val"

        // supplyAsync.join()
        // println "val is $val"
        // assertEquals("Some Value", supplyAsync.get())
    }

    static String example1() {
        CompletableFuture supplyAsync = CompletableFuture
            .supplyAsync( () -> doSomeProcess() )
            // .exceptionally{ ex ->
            //     println "exceptionally ex is $ex"
            // }
            .handle { res, ex ->
                println "handle res is $res"
                println "handle ex is $ex"
            }
            .thenAccept((res) -> println(" thenAccept $res ") )
            // .whenComplete{ res, ex ->
            //     println "whenComplete res is $res"
            //     println "whenComplete ex is $ex"
            // }
    }

    static String doSomeProcess() {
        println "doSomeProcess"
        throw new RuntimeException('some exception')
        // throw new RuntimeException("Example Runtime Foo")
        //return "Hello World"
        // return Promises.delay(1000, Math.random() > 0.5 ?
        //     Promise.of("Hello World") :
        //     Promise.ofException(new RuntimeException("Something went wrong")));
    }
}
