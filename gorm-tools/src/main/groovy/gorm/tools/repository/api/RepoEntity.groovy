/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.api

import groovy.transform.CompileStatic

import gorm.tools.mango.api.QueryMangoEntity
import gorm.tools.model.Persistable

/**
 * Default trait for a domain that has a default Long id and the mango query methods.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait RepoEntity<D> implements BaseRepoEntity<D>, QueryMangoEntity<D>, Persistable<Long> { //PersistableTrait<Long> {

}
