/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic

/**
 * Default trait for a domain that has a generated repo and mango query methods.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait RepoEntity<D> implements PersistableRepoEntity<D, Long> {

}
