/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileDynamic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil

/**
 * Helper for compileStatic and IDE to type the getRepo
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.3
 */
@CompileDynamic
trait HasRepo<D, R extends GormRepo<D>> {

    static R getRepo() { return (R) RepoUtil.findRepo(this) }

    R findRepo() { return (R) RepoUtil.findRepo(getClass()) }
}
