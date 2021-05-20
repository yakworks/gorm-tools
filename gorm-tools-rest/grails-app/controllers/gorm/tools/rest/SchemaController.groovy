/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j

import gorm.tools.openapi.GormToSchema
import grails.converters.JSON

@CompileDynamic
@Slf4j
class SchemaController {

    static namespace = 'api'

    GormToSchema gormToSchema

    def index() {
        log.debug "SchemaController $params"
        //TODO is id is null then what?
        render gormToSchema.generate(params.id) as JSON
    }

}
