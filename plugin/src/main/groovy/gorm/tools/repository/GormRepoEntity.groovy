/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.query.api.BuildableCriteria
import org.grails.datastore.mapping.query.api.Criteria
import org.hibernate.SessionFactory

import gorm.tools.beans.AppCtx
import gorm.tools.hibernate.criteria.GormHibernateCriteriaBuilder
import gorm.tools.mango.api.QueryMangoEntity
import gorm.tools.repository.api.RepositoryApi
import grails.util.Holders

/**
 * Main trait for a domain. gets applied to them during startup grails artifact part
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait GormRepoEntity<D extends GormEntity<D>> implements QueryMangoEntity {

    Class getEntityClass(){ getClass() }

    private static RepositoryApi cachedRepo

    abstract private static GormStaticApi<D> currentGormStaticApi()

    /**
     * finds the repo bean in the appctx if cachedRepo is null. returns the cachedRepo if its already set
     * @return The repository
     */
    static RepositoryApi<D> findRepo() {
        if(!cachedRepo) cachedRepo = AppCtx.get(RepoUtil.getRepoBeanName(this), RepositoryApi)
        return cachedRepo
    }

    /**
     * Calls the findRepo(). can be overriden to return the concrete domain Repository
     * @return The repository
     */
    transient static RepositoryApi<D> getRepo() {
        return findRepo()
    }

    transient static void setRepo(RepositoryApi<D> repo) {
        cachedRepo = repo
    }

    D persist(Map args = [:]) {
        getRepo().persist(args, (D) this)
    }

    void remove(Map args = [:]) {
        getRepo().remove(args, (D) this)
    }

    void bind(Map args = [:], Map data) {
        getRepo().getMapBinder().bind(args, (D) this, data)
    }

    /**
     * Creates, binds and persists and instance
     * @return The created instance
     */
    static D create(Map args = [:], Map data) {
        getRepo().create(args, data)
    }

    static D update(Map args = [:], Map data) {
        getRepo().update(args, data)
    }

    static void removeById(Map args = [:], Serializable id) {
        getRepo().removeById(args, id)
    }

    /**
     * Creates a improved  criteria builder instance
     * make it easier to build criteria with domain bean paths
     * allows
     * eq('invoice.customer.name', 'foo')
     *
     * instead of
     * invoice {
     *      customer {
     *          eq(name)
     *      }
     *  }
     * simliar with eq, like and in
     *
     */
    @Override
    static BuildableCriteria createCriteria() {
        BuildableCriteria builder
        //TODO: temp hack, to prevent unit tests failing
        try {
            builder = new GormHibernateCriteriaBuilder(this, Holders.applicationContext.getBean("sessionFactory", SessionFactory))
            builder.conversionService = currentGormStaticApi().datastore.mappingContext.conversionService
        } catch(IllegalStateException){
            builder = currentGormStaticApi().createCriteria()
        }

        return builder
    }

    static withCriteria(@DelegatesTo(Criteria) Closure callable) {
        createCriteria().invokeMethod("doCall", callable)
    }

}
