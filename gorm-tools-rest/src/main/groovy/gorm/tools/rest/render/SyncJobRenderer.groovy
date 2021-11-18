/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.render

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

import gorm.tools.job.SyncJobEntity
import grails.rest.render.RenderContext

/**
 * Rederer for paged list data
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class SyncJobRenderer implements JsonRendererTrait<SyncJobEntity> {

    @Override
    void render(SyncJobEntity job, RenderContext context) {
        setContentType(context)

        // gets the raw json string and use the unescaped to it just dumps it to writer without any round robin conversion
        String dataString = job.dataToString()
        JsonOutput.JsonUnescaped rawDataJson = JsonOutput.unescaped(dataString)

        jsonBuilder(context).call(
            id: job.id,
            ok:job.ok,
            state:job.state.name(),
            source:job.source,
            sourceId:job.sourceId,
            data: rawDataJson
        )

    }

}
