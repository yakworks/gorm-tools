/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix="yakworks.gorm.id-generator")
@CompileStatic
class IdGeneratorConfig {
    Integer startValue = 1000
    Integer poolBatchSize = 255
}
