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
import gorm.tools.repository.events.AfterMassUpdateEntityEvent
import gorm.tools.repository.events.BeforeMassUpdateEntityEvent
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.utils.ServiceLookup
import yakworks.api.ApiResults
import yakworks.api.HttpStatus
import yakworks.api.Result
import yakworks.api.problem.data.DataProblem
import yakworks.commons.map.Maps
import yakworks.spring.AppCtx

/**
 * Applies the same field changes to many records by id.
 * Each id is updated in its own transaction so failures are collected and the rest still get updated.
 *
 * Register a typed bean with the entity generic, or a subclass of this, and the lookup will use it for that entity.
 * For lighter customization use repo listeners for the before/after mass update entity events
 * and MassUpdateFinishedEvent for batch level work such as creating an activity.
 *
 * @param <D> the entity class this service instance is for
 */
@CompileStatic
class MassUpdateService<D> {

    @Autowired
    ProblemHandler problemHandler

    @Autowired
    RepoEventPublisher repoEventPublisher

    Class<D> entityClass // the domain class this is for

    MassUpdateService(Class<D> entityClass){
        this.entityClass = entityClass
    }

    static <D> MassUpdateService<D> lookup(Class<D> entityClass){
        ServiceLookup.lookup(entityClass, MassUpdateService<D>, "defaultMassUpdateService")
    }

    GormRepo<D> getRepo(){
        return RepoLookup.findRepo(entityClass)
    }

    /**
     * Applies args.data to every id in args.ids.
     *
     * @param args the ids and the shared data map to apply to each of them
     * @return ApiResults with an entry per id, ok for the ones that updated and a problem for the ones that failed
     */
    ApiResults massUpdate(MassUpdateArgs args) {
        if (!args.ids) {
            throw DataProblem.of('error.data.emptyPayload').detail("Mass update ids is empty").toException()
        }
        if (!args.data) {
            throw DataProblem.of('error.data.emptyPayload').detail("Mass update data is empty").toException()
        }

        ApiResults results = ApiResults.create(false)

        for (Object id : args.ids) {
            try {
                Map rowData = prepareData(id, args)
                updateEntity(rowData, args)
                results << Result.OK().payload([id: id]).status(HttpStatus.OK)
            } catch (Exception e) {
                results << problemHandler.handleException(e, entityClass.simpleName).payload([id: id])
            }
        }

        AppCtx.publishEvent(new MassUpdateFinishedEvent<D>(this, entityClass, args, results))
        return results
    }

    /**
     * Builds the data map for one id, clones the shared data so the repo can not mutate it for the other ids.
     */
    protected Map prepareData(Object id, MassUpdateArgs args) {
        Map rowData = Maps.clone(args.data)
        rowData['id'] = id
        return rowData
    }

    /**
     * Updates one entity in its own transaction, override to replace the update path.
     */
    protected D updateEntity(Map rowData, MassUpdateArgs args) {
        PersistArgs pargs = args.persistArgs ? args.persistArgs.clone() : PersistArgs.of()
        //passdown params so repo events can get at them
        if (args.params) {
            pargs.params = args.params
        }

        GormRepo<D> repo = getRepo()

        D entity = null

        repo.withTrx {
            doBeforeMassUpdateEntity(rowData, args)
            entity = repo.update(rowData, pargs)
            doAfterMassUpdateEntity(entity, rowData, args)
        }

      return entity
    }

    /**
     * Called for each item before the doUpdate, inside the trx so it can throw to reject the update.
     */
    protected void doBeforeMassUpdateEntity(Map data, MassUpdateArgs args) {
        BeforeMassUpdateEntityEvent<D> event = new BeforeMassUpdateEntityEvent<D>(getRepo(), data, args)
        repoEventPublisher.publishEvents(getRepo(), event, [event] as Object[])
    }

    /**
     * Called for each item after the doUpdate, inside the trx.
     */
    protected void doAfterMassUpdateEntity(D entity, Map data, MassUpdateArgs args) {
        AfterMassUpdateEntityEvent<D> event = new AfterMassUpdateEntityEvent<D>(getRepo(), entity, data, args)
        repoEventPublisher.publishEvents(getRepo(), event, [event] as Object[])
    }
}
