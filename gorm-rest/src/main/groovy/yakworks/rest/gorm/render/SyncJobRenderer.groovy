/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.job.JobUtils
import gorm.tools.job.SyncJobEntity
import grails.rest.render.RenderContext

/**
 * SyncJob renderer.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@Slf4j
@CompileStatic
class SyncJobRenderer implements JsonRendererTrait<SyncJobEntity> {

    //this is also done in
    @Override
    @CompileDynamic
    void render(SyncJobEntity job, RenderContext context) {
        setContentType(context)

        Map response = JobUtils.convertToMap(job)

        jsonBuilder(context).call(response)
    }

}
