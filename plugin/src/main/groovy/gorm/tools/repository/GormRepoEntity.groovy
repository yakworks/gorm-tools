package gorm.tools.repository

import gorm.tools.beans.AppCtx
import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

import javax.persistence.Transient

@CompileStatic
trait GormRepoEntity<D extends GormEntity<D>> implements MangoRepoEntity {

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
        getRepo().persist(args, (D) this)
    }

    void remove(Map args = [:]) {
        getRepo().remove(args, (D) this)
    }

    /**
     * Creates, binds and persists and instance
     * @return The created instance
     */
    static D create(Map data) {
        getRepo().create(data)
    }

    static D update(Map data) {
        getRepo().update(data)
    }

    static void removeById(Map args = [:], Serializable id) {
        getRepo().removeById(args, id)
    }
}
