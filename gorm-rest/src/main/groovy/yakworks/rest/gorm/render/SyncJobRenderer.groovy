/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.json.JsonOutput
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.job.SyncJobEntity
import grails.rest.render.RenderContext

/**
 * SyncJob renderer.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class SyncJobRenderer implements JsonRendererTrait<SyncJobEntity> {

    @Override
    @CompileDynamic
    void render(SyncJobEntity job, RenderContext context) {
        setContentType(context)

        // gets the raw json string and use the unescaped to it just dumps it to writer without any round robin conversion
        JsonOutput.JsonUnescaped rawDataJson = JsonOutput.unescaped(job.dataToString())

        Map response = [
            id: job.id,
            ok: job.ok,
            state: job.state.name(),
            source: job.source,
            sourceId: job.sourceId,
            data: rawDataJson
        ]

        if(job.errorBytes) {
            response['errors'] = JsonOutput.unescaped(job.errorToString())
        }

        jsonBuilder(context).call(response)
    }

}
