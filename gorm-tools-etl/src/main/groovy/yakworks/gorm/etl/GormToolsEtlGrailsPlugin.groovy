/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.etl

import grails.plugins.Plugin

@SuppressWarnings(['Indentation'])
class GormToolsEtlGrailsPlugin extends Plugin {

    def loadAfter = ['gorm-tools']
    def pluginExcludes = ["**/init/**"]

    Closure doWithSpring() { { ->

    } }

}
