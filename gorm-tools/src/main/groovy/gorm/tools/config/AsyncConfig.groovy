/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.config

import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

import yakworks.spring.SpringEnvironment

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix="gorm.tools.async")
// @ConfigurationPropertiesScan
@CompileStatic
class AsyncConfig implements EnvironmentAware {

    private Environment env

    /**
     * The default slice or chunk size for collating. for example if this is 100 and you pass list of of 100
     * then it will slice it or collate it into a list with 10 of lists with 100 items each.
     * should default to the hibernate.jdbc.batch_size in the implementation. Usually best to set this around 100
     */
    int sliceSize = 100

    /** The list size to send to the collate that slices.*/
    boolean enabled = true

    /** the pool size, defaults to 4 right now, parralel gets it own their own */
    int poolSize = 4

    /** setup defaults for poolSize and batchSize if config isn't present. batchSize set to 100 if not config found*/
    @PostConstruct
    void init() {
        //if batchSize is 0 then hibernate may not bbe installed and hibernate.jdbc.batch_size is not set. force it to 100
        Integer batchSize = env.getProperty('hibernate.jdbc.batch_size', Integer)
        sliceSize = batchSize ?: sliceSize
    }

    /**
     * Set the {@code Environment} that this component runs in.
     */
    void setEnvironment(Environment environment){
        env = environment
    }
}
