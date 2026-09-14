/*
* Copyright 2026 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.massupdate

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoLookup
import yakworks.api.ApiResults
import yakworks.api.HttpStatus
import yakworks.api.Result
import yakworks.api.problem.data.DataProblem
import yakworks.commons.map.Maps
import yakworks.gorm.api.bulk.AfterMassUpdateEvent
import yakworks.spring.AppCtx

/**
 * Applies the same field changes to many records by id.
 * One transaction per id so failures are collected and others still update.
 */
@CompileStatic
class MassUpdateService {

    @Autowired
    ProblemHandler problemHandler

    /**
     * Mass update entities of the given class.
     *
     * @param entityClass domain class
     * @param args ids + shared data map
     * @return ApiResults with per-id success or problem entries
     */
    ApiResults massUpdate(Class entityClass, MassUpdateArgs args) {
        if (!args.ids) {
            throw DataProblem.of('error.data.emptyPayload').detail("Mass update ids is empty").toException()
        }
        if (!args.data) {
            throw DataProblem.of('error.data.emptyPayload').detail("Mass update data is empty").toException()
        }

        doBeforeMassUpdate(entityClass, args)

        ApiResults results = ApiResults.create(false)
        GormRepo repo = RepoLookup.findRepo(entityClass)

        for (Object id : args.ids) {
            try {
                Map rowData = prepareData(entityClass, id, args.data, args)
                updateEntity(repo, entityClass, id, rowData, args)
                results << Result.OK().payload([id: id]).status(HttpStatus.OK)
            } catch (Exception e) {
                results << problemHandler.handleException(e, entityClass.simpleName).payload([id: id])
            }
        }

        doAfterMassUpdate(entityClass, args, results)
        AppCtx.publishEvent(new AfterMassUpdateEvent(this, entityClass, args, results))
        return results
    }

    /**
     * Build the data map for one id. Default clones shared data and sets id.
     */
    protected Map prepareData(Class entityClass, Object id, Map data, MassUpdateArgs args) {
        Map rowData = Maps.clone(data)
        rowData['id'] = id
        return rowData
    }

    /**
     * Update one entity in its own transaction. Override to replace the update path.
     */
    protected void updateEntity(GormRepo repo, Class entityClass, Object id, Map rowData, MassUpdateArgs args) {
        PersistArgs pargs = args.persistArgs ? args.persistArgs.clone() : PersistArgs.of()
        if (args.params) {
            pargs.params = args.params
        }
        repo.update(rowData, pargs)
    }

    /** Override for batch-level setup before any updates. */
    protected void doBeforeMassUpdate(Class entityClass, MassUpdateArgs args) {
        // no-op
    }

    /** Override for batch-level work after all ids (e.g. activity). Prefer AfterMassUpdateEvent for loose coupling. */
    protected void doAfterMassUpdate(Class entityClass, MassUpdateArgs args, ApiResults results) {
        // no-op
    }
}
