/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileStatic

import gorm.tools.repository.artefact.RepositoryArtefactHandler
import grails.util.Environment
import yakworks.commons.lang.NameUtils
import yakworks.spring.AppCtx

/**
 * Cache and finders to make looking up Repo faster.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7
 */
@SuppressWarnings(['FieldName'])
@CompileStatic
@SuppressWarnings(["FieldName"])
class RepoLookup {

    private static final Map<String, GormRepo> REPO_CACHE = new ConcurrentHashMap<String, GormRepo>()
    //set to false when doing unit tests so it doesnt cache old ones
    public static Boolean USE_CACHE

    protected static Boolean shouldCache(){
        //if reload enabled then dont cache
        if(USE_CACHE == null) USE_CACHE = !Environment.getCurrent().isReloadEnabled()
        return USE_CACHE
    }

    /**
     * Lookup repo in the cache, if not found then uses getRepoBeanName(entityClass) to find bean
     * in the applicationContext.
     */
    protected static <D> GormRepo<D> findRepoCached(Class<D> entity) {
        String className = NameUtils.getClassName(entity)
        def repo = REPO_CACHE.get(className)
        if(repo == null) {
            repo = getRepoFromAppContext(entity)
            REPO_CACHE.put(className, repo)
        }
        return repo as GormRepo<D>
    }

    protected static <D> GormRepo<D> getRepoFromAppContext(Class<D> entity){
        return AppCtx.get(getRepoBeanName(entity), GormRepo) as GormRepo<D>
    }

    static String getRepoBeanName(Class domainClass) {
        RepositoryArtefactHandler.getRepoBeanName(domainClass)
    }

    /**
     * primary method to find the repo in the cache
     */
    static <D> GormRepo<D> findRepo(Class<D> entity) {
        if(shouldCache()){
            return findRepoCached(entity)
        } else {
            return getRepoFromAppContext(entity)
        }
    }
}
