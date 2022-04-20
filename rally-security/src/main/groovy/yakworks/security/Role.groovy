/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileStatic

import gorm.tools.security.domain.SecRole
import yakworks.commons.lang.EnumUtils
import yakworks.commons.model.IdEnum

@CompileStatic
enum Role implements IdEnum<Role, Long> {
    ADMIN(1), //full access, system user
    //default user, access to all the screens, not manager (cannot approve, view other's tasks or delete cash system data)
    POWER_USER(2),
    //access to all user's tasks, approval, can delete cash system data
    MANAGER(3),
    //read only
    GUEST(4),
    //greenbill single customer user
    CUSTOMER(5),
    AR_COLLECTOR(6),
    AR_MANAGER(7), //can see other collector tasks, approvals
    AUTOCASH(8),
    AUTOCASH_MANAGER(9),
    AUTOCASH_OFFSET(10),
    ADMIN_CONFIG(11),
    ADMIN_SEC(12),
    SALES(13),
    BRANCH(14),
    GL_ADMIN(15),
    GL_READ_ONLY(16)

    final Long id

    Role(Long id) {
        this.id = id
    }

    SecRole getTypeSetup() {
        return SecRole.get(id)
    }

    //get the user customizable name from domain
    String getName(){
        getTypeSetup().name
    }

    /**
     * case insensitive getter, will convert object to string
     */
    static Role get(Object name){
        EnumUtils.getEnumIgnoreCase(Role, name.toString())
    }
}
