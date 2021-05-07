/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil

/**
 * core trait for repo methods that use the repo for persistance
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait PersistableRepoEntity<D, R extends GormRepo<D>> implements HasRepo<D, R>, Persistable {

    static R getRepo() { return (R) RepoUtil.findRepo(this) }

    R findRepo() { return (R) RepoUtil.findRepo(getClass()) }

    D persist(Map args = [:]) {
        return findRepo().persist((D) this, args)
    }

    void remove(Map args = [:]) {
        findRepo().remove((D) this, args)
    }

    void bind(Map args = [:], Map data) {
        findRepo().getMapBinder().bind(args, (D) this, data)
    }

    /**
     * Creates, binds and persists and instance
     * @return The created instance
     */
    static D create(Map args = [:], Map data) {
        return getRepo().create(data, args)
    }

    static D update(Map args = [:], Map data) {
        getRepo().update(data, args)
    }

    static void removeById(Map args = [:], Serializable id) {
        getRepo().removeById(id, args)
    }

    // this will fire and event and call beforeValidate on the repo.
    // when cascading to child associations while validated in grail's gorm it doesn't fire a ValidationEvent
    // so we fire our own here. FIXME see line 231 in PersistentEntityValidator where it should fire event and issue PR
    // void beforeValidate() {
    //     getRepo().publishBeforeValidate(this)
    // }
}
