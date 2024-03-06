/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.json.JsonOutput
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

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

    @Override
    @CompileDynamic
    void render(SyncJobEntity job, RenderContext context) {
        setContentType(context)

        // gets the raw json string and use the unescaped to it just dumps it to writer without any round robin conversion
        String jobData = job.dataToString()
        JsonOutput.JsonUnescaped rawDataJson = JsonOutput.unescaped(jobData)

        //if its null/empty or has just 2 chars eg {}, or [], thn log it
        if(job.ok && job.sourceId.contains('exportSync') && (!jobData || jobData.length() < 3)) {
            log.warn("Syncjob#${job['id']} with empty data")
        }

        Map response = [
            id: job.id,
            ok: job.ok,
            state: job.state.name(),
            source: job.source,
            sourceId: job.sourceId,
            data: rawDataJson
        ]

        if(job.problems) {
            response['problems'] = job.problems
        }

        jsonBuilder(context).call(response)
    }

}
