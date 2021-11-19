/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.core.Datastore
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.interceptor.TransactionAspectSupport

import gorm.tools.beans.AppCtx
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import yakworks.problem.Problem
import yakworks.problem.ProblemException
import yakworks.problem.data.EntityNotFoundProblem
import yakworks.problem.data.OptimisticLockingProblem

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
        AppCtx.grails.getArtefacts(RepositoryArtefactHandler.TYPE)*.clazz
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
                throw OptimisticLockingProblem
                    .of(entity)
                    .detail("server version:${currentVersion} > edited version:${oldVersion}")
            }
        }
    }

    /**
     * check that the passed in entity is not null and throws EntityNotFoundException if so
     *
     * @param entity - the domain object the check
     * @param id - the identifier use when trying to find it. Will be used to construct the exception message
     * @param domainClassName - the name of the domain that will be used to build error message if thrown
     * @throws EntityNotFoundProblem if it not found
     */
    static void checkFound(Object entity, Serializable id, String domainClassName) {
        if (!entity) {
            throw EntityNotFoundProblem.of(id, domainClassName)
        }
    }

    /**
     * check that the passed in data is not empty and throws EmptyDataException if so
     * @throws ProblemException if it not found
     */
    static void checkData(Map data, Class entityClass) {
        if (!data) {
            throw Problem.of('error.data.empty', [name: entityClass.simpleName])
        }
    }

    /**
     * in create data, if id is passed then bindId must be set to true, if not throw exception
     */
    static void checkCreateData(Map data, Map args, Class entityClass) {
        if(data['id'] && !args.bindId)
            throw Problem.of('error.data.empty', [name: entityClass.simpleName])
    }

    /**
     * force a roll back if in a transaction
     */
    static void rollback() {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
    }

    /**
     * flushes the session and clears the session cache and the DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
     */
    static void flushAndClear() {
        flush()
        clear()
    }

    /**
     * flushes the session
     */
    @CompileDynamic
    @Deprecated
    static void flush() {
        Datastore ds = GormEnhancer.findSingleDatastore()
        if(ds.hasCurrentSession()) ds.getCurrentSession().flush()
    }

    /**
     * clears the session cache
     */
    @CompileDynamic
    @Deprecated
    static void clear() {
        Datastore ds = GormEnhancer.findSingleDatastore()
        if(ds.hasCurrentSession()) ds.getCurrentSession().clear()
    }

    @CompileDynamic
    static void flushAndClear(TransactionStatus status) {
        //TransactionObject txObject = (status as DefaultTransactionStatus).transaction as TransactionObject
        status.flush()
        clear(status)
    }

    @CompileDynamic
    static void clear(TransactionStatus status) {
        status.transaction.sessionHolder.getSession().clear()
    }

    /**
     * no used right now, but kept for refernce
     */
    // public <D> List<D> doAssociation(D entity, Class associatedEntityClass, List<Map> assocList) {
    //     PersistentEntity associatedEntity = GormMetaUtils.getPersistentEntity(associatedEntityClass)
    //     GormRepo assocRepo = RepoUtil.findRepo(associatedEntity.javaClass)
    //
    //     //if the associated entity has a reference to entity, set it on data map.
    //     //eg. set contact.org = org
    //     PersistentProperty p = associatedEntity.getPropertyByName(NameUtils.getPropertyName(entity.class))
    //     doAssociation(entity, assocRepo, assocList, p?.name)
    // }

}
