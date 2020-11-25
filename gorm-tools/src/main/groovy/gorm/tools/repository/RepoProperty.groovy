/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

/**
 * Helper for compileStatic and IDE to type the getRepo
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.3
 */
trait RepoProperty<T extends GormRepo> {

    static T getRepo() {
        return findRepo() as T
    }

}
