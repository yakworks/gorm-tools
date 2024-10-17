/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest


import org.grails.datastore.gorm.validation.constraints.registry.DefaultConstraintRegistry

import grails.plugins.Plugin
import yakworks.rest.gorm.mapping.RepoApiMappingsService
import yakworks.rest.gorm.render.ApiResultsRenderer
import yakworks.rest.gorm.render.CSVPagerRenderer
import yakworks.rest.gorm.render.JsonGeneratorRenderer
import yakworks.rest.gorm.render.PagerRenderer
import yakworks.rest.gorm.render.ProblemRenderer
import yakworks.rest.gorm.render.SyncJobRenderer
import yakworks.rest.gorm.render.XlsxPagerRenderer
import yakworks.rest.gorm.responder.RestResponderService

@SuppressWarnings(['UnnecessarySelfAssignment', 'Println', 'EmptyMethod', 'Indentation'])
class GormRestGrailsPlugin extends Plugin {

    def loadAfter = ['gorm-openapi']
    //make sure we load before controllers as might be creating rest controllers
    def loadBefore = ['controllers']
    def pluginExcludes = ["**/init/**"]

    Closure doWithSpring() { {->

        tomcatWebServerCustomizer(RestTomcatWebServerCustomizer)
        //setup to try and speed up constraint eval so its only setup once.
        urlMappingsConstraintRegistry(DefaultConstraintRegistry, ref('messageSource'))
        //the default UrlMappings calls this.
        repoApiMappingsService(RepoApiMappingsService){
            // FIXME @Autowired is not working on RepoApiMappingsService during dev, works when run from tests or in prod.
            // doing this for now until we sort out why.
            grailsApplication = ref('grailsApplication')
            urlMappingsConstraintRegistry = ref('urlMappingsConstraintRegistry')
        }
        restResponderService(RestResponderService)
        //renderers
        mapJsonRenderer(JsonGeneratorRenderer, Map)
        apiResultsRenderer(ApiResultsRenderer)
        problemRenderer(ProblemRenderer)
        pagerRenderer(PagerRenderer)
        syncJobRenderer(SyncJobRenderer)

        csvPagerRenderer(CSVPagerRenderer)
        xlsxPagerRenderer(XlsxPagerRenderer)
    } }

}
