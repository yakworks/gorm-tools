package gorm.tools.repository

import gorm.tools.beans.AppCtx
import gorm.tools.mango.api.QueryMangoEntity
import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

@CompileStatic
trait GormRepoEntity<D extends GormEntity<D>> implements QueryMangoEntity {

    Class getEntityClass(){ getClass() }

    private static RepositoryApi cachedRepo

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
}
