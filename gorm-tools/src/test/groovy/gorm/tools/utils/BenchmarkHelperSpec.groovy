/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils

import spock.lang.Specification

class BenchmarkHelperSpec extends Specification {

    def "elapsed time"() {
        expect:
        BenchmarkHelper.elapsedTime(System.currentTimeMillis() - 511) == "0.5s"
        BenchmarkHelper.elapsedTime(System.currentTimeMillis() - 1000) == "1.0s"
        BenchmarkHelper.elapsedTime(System.currentTimeMillis() - 10500) == "10.5s"
    }

    def "getUsedMem"() {
        expect:
        BenchmarkHelper.getUsedMem().endsWith("GB")
    }

}
