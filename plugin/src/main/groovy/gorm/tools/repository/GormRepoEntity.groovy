package gorm.tools.repository

import gorm.tools.beans.AppCtx
import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

import javax.persistence.Transient

@CompileStatic
trait GormRepoEntity<D extends GormEntity<D>> {

    @Transient
    private static RepositoryApi cachedRepo

    /**
     * Looks up and caches a repository bean
     * @return The repository
     */
    static RepositoryApi<D> getRepo() {
        if(!cachedRepo) cachedRepo = AppCtx.get(RepoUtil.getRepoBeanName(this), RepositoryApi)
        return cachedRepo
    }

    static void setRepo(RepositoryApi<D> repo) {
        cachedRepo = repo
    }

    D persist(Map args = [:]) {
        getRepo().persist((D) this, args)
    }

    /**
     * Creates, binds and persists and instance
     * @return The created instance
     */
    static D create(Map params) {
        getRepo().create(params)
    }

    static D update(Map params) {
        getRepo().update(params)
    }

    void remove() {
        getRepo().remove((D) this)
    }

    static void remove(Serializable id) {
        getRepo().removeById(id)
    }
}
