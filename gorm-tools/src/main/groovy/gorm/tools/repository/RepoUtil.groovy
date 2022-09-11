/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import groovy.transform.CompileStatic

import org.springframework.dao.OptimisticLockingFailureException

import gorm.tools.repository.artefact.RepositoryArtefactHandler
import gorm.tools.transaction.TrxService
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemCodes
import yakworks.api.problem.data.NotFoundProblem
import yakworks.grails.GrailsHolder

/**
 * A bunch of statics to support the repositories.
 * this is also setup as repoUtilBean so that it gets injected with the ApplicationContext once its setup
 *
 * @author Joshua Burnett (@basejump)
 * @since 1.0
 */
@SuppressWarnings(['FieldName'])
@CompileStatic
@SuppressWarnings(["FieldName"])
class RepoUtil {

    static List<Class> getRepoClasses(){
        GrailsHolder.grailsApplication.getArtefacts(RepositoryArtefactHandler.TYPE)*.clazz
    }

    static String getRepoBeanName(Class domainClass) {
        RepositoryArtefactHandler.getRepoBeanName(domainClass)
    }

    /**
     * checks the passed in version with the version on the entity (entity.version)
     * make sure entity.version is not greater
     *
     * @param entity the domain object the check
     * @param ver the version this used to be (entity will have the )
     * @throws OptimisticLockingFailureException
     */
    static void checkVersion(Object entity, Long oldVersion) {
        if (oldVersion == null) return
        if (entity.hasProperty('version')) {
            Long currentVersion = entity['version'] as Long
            if (currentVersion > oldVersion) {
                throw DataProblemCodes.OptimisticLocking.get()
                    .entity(entity)
                    .detail("server version:${currentVersion} > edited version:${oldVersion}")
                    .toException()
            }
        }
    }

    /**
     * check that the passed in entity is not null and throws EntityNotFoundException if so
     *
     * @param entity - the domain object the check
     * @param id - the identifier use when trying to find it. Will be used to construct the exception message
     * @param domainClassName - the name of the domain that will be used to build error message if thrown
     * @throws NotFoundProblem if it not found
     */
    static void checkFound(Object entity, Serializable id, String domainClassName) {
        if (!entity) {
            throw NotFoundProblem.of(id, domainClassName).toException()
        }
    }

    /**
     * check that the passed in data is not empty and throws EmptyDataException if so
     * @throws ProblemRuntime if it not found
     */
    static void checkData(Map data, Class entityClass) {
        if (!data) {
            throw DataProblem.of('error.data.empty', [name: entityClass.simpleName]).toException()
        }
    }

    /**
     * in create data, if id is passed then bindId must be set to true, if not throw exception
     */
    static void checkCreateData(Map data, PersistArgs args, Class entityClass) {
        if(data['id'] && !args.bindId)
            throw DataProblem.of('error.data.bindId', [name: entityClass.simpleName])
                .title("set bindId:true when manually assigning id in create data").toException()
    }

    /**
     * flushes the session and clears the session cache and the DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
     */
    @Deprecated
    static void flushAndClear() {
        flush()
        clear()
    }

    /**
     * flushes the session
     */
    @Deprecated
    static void flush() {
        TrxService.bean().flush()
    }

    /**
     * clears the session cache
     */
    @Deprecated
    static void clear() {
        TrxService.bean().clear()
    }

}
