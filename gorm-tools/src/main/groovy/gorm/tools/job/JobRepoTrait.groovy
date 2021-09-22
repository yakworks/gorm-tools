/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import gorm.tools.json.Jsonify
import gorm.tools.repository.GormRepo
import gorm.tools.repository.bulk.BulkableResults
import yakworks.commons.lang.Validate


@CompileStatic
trait JobRepoTrait<D extends JobTrait<D>> implements GormRepo<D> {

    D create(String source, String sourceId, Object payload) {
        Validate.notNull(payload)
        byte[] data = Jsonify.render(payload).jsonText.bytes
        return create([source: source, sourceId: sourceId, state: JobState.Running, requestData: data], [flush:true])
    }

    D update(Long id, JobState state, BulkableResults results, List<Map> renderResults) {
        byte[] resultBytes = Jsonify.render(renderResults).jsonText.bytes
        return update([id:id, ok: results.ok, data: resultBytes, state: state], [flush: true])
    }
}
