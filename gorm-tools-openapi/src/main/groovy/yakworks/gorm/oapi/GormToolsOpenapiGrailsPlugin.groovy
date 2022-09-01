/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.oapi

import grails.plugins.Plugin
import yakworks.gorm.oapi.meta.MetaEntitySchemaService

@SuppressWarnings(['Indentation'])
class GormToolsOpenapiGrailsPlugin extends Plugin {
    def loadAfter = ['gorm-tools']

    Closure doWithSpring() { {->

        gormToSchema(GormToSchema) { bean ->
            bean.lazyInit = true
        }

        openApiGenerator(OpenApiGenerator) { bean ->
            bean.lazyInit = true
            apiSrc = 'src/api-docs'
            apiBuild = 'build/api-docs'
            namespaceList = ['rally']
        }

        //overrides the metaEntityService
        metaEntityService(MetaEntitySchemaService)

    } }
}
