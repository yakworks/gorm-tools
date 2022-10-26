/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import gorm.tools.repository.model.AbstractCrossRefRepo

@GormRepository
@CompileStatic
class SecRoleUserRepo extends AbstractCrossRefRepo<SecRoleUser, SecRole, AppUser> {

    SecRoleUserRepo(){
        super(SecRole, AppUser, ['role', 'user'] )
    }

}
