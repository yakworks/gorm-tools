package gorm.tools.repository

import gorm.tools.beans.AppCtx
import gorm.tools.mango.api.MangoQueryEntity
import gorm.tools.mango.api.MangoQueryTrait
import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

@CompileStatic
trait GormRepoEntity<D extends GormEntity<D>> implements MangoQueryEntity {

    private static RepositoryApi cachedRepo

    /**
     * Looks up and caches a repository bean
     * @return The repository
     */
    transient static RepositoryApi<D> getRepo() {
        if(!cachedRepo) cachedRepo = AppCtx.get(RepoUtil.getRepoBeanName(this), RepositoryApi)
        return cachedRepo
    }

    transient static void setRepo(RepositoryApi<D> repo) {
        cachedRepo = repo
    }

    static MangoQueryTrait getMangoQueryTrait(){
        (MangoQueryTrait)getRepo()
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
