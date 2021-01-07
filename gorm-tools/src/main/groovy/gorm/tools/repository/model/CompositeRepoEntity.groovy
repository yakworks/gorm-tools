/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

import gorm.tools.mango.api.QueryMangoEntity
import gorm.tools.repository.GormRepo

/**
 * A trait for an entity that has a composite id.
 */
@CompileStatic
trait CompositeRepoEntity<D> implements PersistableRepoEntity<D, GormRepo<D>, Long>, QueryMangoEntity<D> {

}
