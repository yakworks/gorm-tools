/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import gorm.tools.json.Jsonify
import gorm.tools.repository.GormRepo


@CompileStatic
trait JobRepoTrait<D extends JobTrait<D>> implements GormRepo<D> {

    /**
     * Assigns data bytes array with json if passed in as 'dataPayload'
     * @param data data map
     * @return Job created job
     */
    D createJob(Map data ) {
        def dataPayload = data.remove('dataPayload')
        D job = (D) getEntityClass().newInstance(data)

        // must be Job called from RestApi that is passing in dataPayload
        if (dataPayload) {
            if(dataPayload instanceof Map) {
                def res = Jsonify.render(dataPayload)
                job.data = res.jsonText.bytes
            } else {
                def bytes = dataPayload.toString().bytes
                job.data = bytes
            }
        }
        return job
    }
}
