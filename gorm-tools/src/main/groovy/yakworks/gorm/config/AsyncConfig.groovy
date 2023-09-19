/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.config


import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix="gorm.tools.async")
// @ConfigurationPropertiesScan
@CompileStatic
class AsyncConfig {

    // batchSize will override and set the sliceSize
    @Value('${hibernate.jdbc.batch_size:0}')
    private int batchSize

    /**
     * The default slice or chunk size for collating. for example if this is 100 and you pass list of of 100
     * then it will slice it or collate it into a list with 10 of lists with 100 items each.
     * should default to the hibernate.jdbc.batch_size in the implementation. Usually best to set this around 100
     */
    int sliceSize

    /** The list size to send to the collate that slices.*/
    boolean enabled = true

    /** the pool size, defaults to 4 right now, parralel gets it own their own */
    int poolSize = 4

    int getSliceSize(){
        if(!sliceSize) sliceSize = batchSize ?: 100
        return this.sliceSize
    }

}
