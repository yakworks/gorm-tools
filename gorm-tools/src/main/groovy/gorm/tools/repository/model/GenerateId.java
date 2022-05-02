/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model;

import gorm.tools.model.Persistable;

/**
 * Simple interface for id generator
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.3
 */
public interface GenerateId<ID> {

    ID generateId();

    ID generateId(Persistable<ID> entity);
}
