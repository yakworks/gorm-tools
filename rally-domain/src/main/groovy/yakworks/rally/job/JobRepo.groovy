/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job


import groovy.transform.CompileStatic

import gorm.tools.json.JsonTools
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.source.SourceType

@GormRepository
@CompileStatic
class JobRepo implements GormRepo<Job> {

    @RepoListener
    void beforeBind(Job job, Map data, BeforeBindEvent be) {
        if (be.isBindCreate()) {
            // must be Job called from RestApi that is passing in dataPayload
            def payload = data.dataPayload
            if (payload  && (payload instanceof Map || payload instanceof List)) {
                String res = JsonTools.toJson(payload)
                job.requestData = res.bytes
                job.sourceType = SourceType.RestApi  // we should default to RestApi if dataPayload is passed
            }
        }
    }
}
