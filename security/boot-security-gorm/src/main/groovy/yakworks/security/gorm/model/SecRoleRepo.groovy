/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.AbstractCrossRefRepo
import gorm.tools.repository.model.LongIdGormRepo

@GormRepository
@CompileStatic
class SecRoleRepo extends LongIdGormRepo<SecRole> {

    /**
     * Remove permissions from data map and bind before databinding, databinder doesnt handle lists
     */
    @RepoListener
    void beforeBind(SecRole role, Map data, BeforeBindEvent e) {
        if(data && data['permissions']) {
            role.permissions = data.remove('permissions') as List<String>
        }
    }
}
