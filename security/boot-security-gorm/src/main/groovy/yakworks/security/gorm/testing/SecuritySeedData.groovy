/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.testing

import groovy.transform.CompileStatic

import yakworks.security.Roles
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRolePermission

@CompileStatic
class SecuritySeedData {

    static void fullMonty(){
        createRoles()
        createAppUsers()
    }

    static void createAppUsers(){

        AppUser admin = new AppUser([
            id: (Long)1, username: "admin", email: "admin@yak.com", password:"123", orgId: 2
        ]).persist()

        admin.addRole(Roles.ADMIN, true)
        admin.addRole(Roles.MANAGER, true)

        AppUser custUser = new AppUser([
            id: (Long)2, username: "cust", email: "cust@yak.com", password:"123", orgId: 2
        ]).persist()
        assert custUser.id == 2

        custUser.addRole(Roles.CUSTOMER, true)

        AppUser noRoleUser = AppUser.create([id: 3L, username: "noroles", email: "noroles@yak.com", password:"123", orgId: 3], bindId: true)
        assert noRoleUser.id == 3
        SecRole.repo.flush()
    }

    static void createRoles(){

        SecRole admin = new SecRole(id: (Long)1, code: Roles.ADMIN).persist()
        SecRole power = new SecRole(id: (Long)2, code: Roles.POWER_USER).persist()
        SecRole mgr = new SecRole(id: (Long)3, code: Roles.MANAGER).persist()
        SecRole custRole = new SecRole(id: (Long)5, code: Roles.CUSTOMER).persist()
        // SecRole foo = new SecRole(id: (Long)6, code: "FOO").persist()

        //add permissions
        adminPermissions(admin)
        custPermissions(custRole)
        SecRole.repo.flush()
    }

    static void adminPermissions(SecRole role){

        ['rally:org:*',
         'rally:activityNote:*',
         'rally:company:list,get,post',
         'rally:orgTypeSetup:list,get,post',
         'rally:syncJob:list,get',
         'rally:user:*',
         'rally:role:read',
         'rally:contact:*',
         'rally:activity:*',
         'rally:attachment:*',
         'rally:tag:*'
        ].each{
            SecRolePermission.create(role, it)
        }
    }

    static void custPermissions(SecRole role){
        ['rally:org:list,get,post',
         // 'rally:contact:*',
         'rally:activity:list,get',
         'rally:attachment:*',
         'rally:tag:list,get'
        ].each{
            SecRolePermission.create(role, it)
        }
    }

}
