/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

import gorm.tools.mango.api.QueryMangoEntity
import gorm.tools.repository.GormRepo

/**
 * Default trait for a domain that has a concrete implemented repo and mango query methods.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait GormRepoEntity<D, R extends GormRepo<D>> implements PersistableRepoEntity<D, R, Long>, QueryMangoEntity<D> {

}
