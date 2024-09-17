/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo

@CompileStatic
trait StringIdRepoEntity<D, R extends GormRepo<D>> implements PersistableRepoEntity<D, R, String> {

}
