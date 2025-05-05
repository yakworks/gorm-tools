/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.config

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties([AsyncConfig, GormConfig, IdGeneratorConfig, QueryConfig])
@CompileStatic
class GormToolsPropertiesConfiguration {

}
