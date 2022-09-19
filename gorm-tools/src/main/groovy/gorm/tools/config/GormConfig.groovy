/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.config

import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

import yakworks.spring.SpringEnvironment

@Configuration @ConfigurationProperties(prefix="gorm.tools")
@CompileStatic
class GormConfig implements SpringEnvironment{

    /** almost never would this be false if including it unless turning off for a test */
    boolean enabled = true
    String hello

    Async async = new Async()

    static class Async {
        /** Wether to dfault to using it*/
        boolean enabled = true
        /**
         * The default slice or chunk size for collating. for example if this is 100 and you pass list of of 100
         * then it will slice it or collate it into a list with 10 of lists with 100 items each.
         * should default to the hibernate.jdbc.batch_size in the implementation. Usually best to set this around 100
         */
        Integer sliceSize = 100
        /** the pool size, defaults to 4 right now, parralel gets it own their own */
        Integer poolSize = 4
    }

    //TODO wip, move config to here for the idgen.
    IdGenerator idGenerator = new IdGenerator()

    static class IdGenerator {
        Integer startValue = 1000
        Integer poolBatchSize = 255
    }

    /** setup defaults for poolSize and batchSize if config isn't present. batchSize set to 100 if not config found*/
    @PostConstruct
    void init() {
        //if batchSize is 0 then hibernate may not bbe installed and hibernate.jdbc.batch_size is not set. force it to 100
        Integer batchSize = environment.getProperty('hibernate.jdbc.batch_size', Integer)
        async.sliceSize = batchSize ?: async.sliceSize
    }
}
