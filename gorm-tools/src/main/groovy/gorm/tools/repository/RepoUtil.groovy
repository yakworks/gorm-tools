/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.mapping.core.Datastore
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.interceptor.TransactionAspectSupport

import gorm.tools.beans.AppCtx
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import gorm.tools.repository.errors.EntityNotFoundException
import grails.util.Environment
import yakworks.commons.lang.NameUtils

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
class  RepoUtil {

    private static final Map<String, GormRepo> REPO_CACHE = new ConcurrentHashMap<String, GormRepo>()
    //set to false when doing unit tests so it doesnt cache old ones
    public static Boolean USE_CACHE

    static Boolean shouldCache(){
        //if reload enabled then dont cache
        if(USE_CACHE == null) USE_CACHE = !Environment.getCurrent().isReloadEnabled()
        return USE_CACHE
    }

    static <D> GormRepo<D> findRepoCached(Class<D> entity) {
        String className = NameUtils.getClassName(entity)
        def repo = REPO_CACHE.get(className)
        if(repo == null) {
            repo = getRepoFromAppContext(entity)
            REPO_CACHE.put(className, repo)
        }
        return repo as GormRepo<D>
    }

    static <D> GormRepo<D> findRepo(Class<D> entity) {
        if(shouldCache()){
            return findRepoCached(entity)
        } else {
            return getRepoFromAppContext(entity)
        }
    }

    static List<Class> getRepoClasses(){
        AppCtx.grails.getArtefacts(RepositoryArtefactHandler.TYPE)*.clazz
    }

    static <D> GormRepo<D> getRepoFromAppContext(Class<D> entity){
        return AppCtx.get(getRepoBeanName(entity), GormRepo) as GormRepo<D>
    }

    static String getRepoClassName(Class domainClass) {
        RepositoryArtefactHandler.getRepoClassName(domainClass)
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
                def msgKey = RepoMessage.optimisticLockingFailure(entity)
                throw new OptimisticLockingFailureException(msgKey.defaultMessage)
            }
        }
    }

    /**
     * check that the passed in entity is not null and throws EntityNotFoundException if so
     *
     * @param entity - the domain object the check
     * @param id - the identifier use when trying to find it. Will be used to construct the exception message
     * @param domainClassName - the name of the domain that will be used to build error message if thrown
     * @throws EntityNotFoundException if it not found
     */
    static void checkFound(Object entity, Serializable id, String domainClassName) {
        if (!entity) {
            throw new EntityNotFoundException(id, domainClassName)
        }
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
    @Deprecated
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

}
