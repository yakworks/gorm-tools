/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model


import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo

/**
 * A trait that adds id generator to repo for manually generating ids during validation and for persist
 * instead of waiting for hibernate to generate it. Used when associations get messy and for performance when inserting.
 * uses the default Long type from IdGenerator bean
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.3
 */
@CompileStatic
class LongIdGormRepo<D> implements GormRepo<D>, IdGeneratorRepo<D> {

}
