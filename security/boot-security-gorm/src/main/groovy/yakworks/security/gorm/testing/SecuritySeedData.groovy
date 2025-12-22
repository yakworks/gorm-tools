/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.testing

import groovy.transform.CompileStatic

import yakworks.security.Roles
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole

@CompileStatic
class SecuritySeedData {

    static void fullMonty(){
        createRoles()
        createAppUsers()
    }

    static void createAppUsers(){

        AppUser admin = new AppUser([id: 1L, username: "admin", email: "admin@yak.com", password:"123", orgId: 2]).persist()

        admin.addRole(Roles.ADMIN, true)
        admin.addRole(Roles.MANAGER, true)

        AppUser custUser = new AppUser([id: 2L, username: "cust", email: "cust@yak.com", password:"123", orgId: 2]).persist()
        assert custUser.id == 2

        custUser.addRole(Roles.CUSTOMER, true)

        AppUser noRoleUser = AppUser.repo.create(
            [id: 3L, username: "noroles", email: "noroles@yak.com", password:"123", orgId: 3],
            [bindId: true]
        )
        assert noRoleUser.id == 3

        AppUser readonlyUser = new AppUser([id: 4L, username: "readonly", email: "readonly@yak.com", password:"123", orgId: 2]).persist()
        assert readonlyUser.id == 4
        readonlyUser.addRole(Roles.READ_ONLY, true)

        SecRole.repo.flush()
    }

    static void createRoles(){

        SecRole admin = new SecRole(id: 1L, code: Roles.ADMIN).persist()
        SecRole power = new SecRole(id: 2L, code: Roles.POWER_USER).persist()
        SecRole mgr = new SecRole(id: 3L, code: Roles.MANAGER).persist()
        SecRole custRole = new SecRole(id: 5L, code: Roles.CUSTOMER).persist()
        SecRole readonly = new SecRole(id: 6L, code: Roles.READ_ONLY).persist()

        //add permissions
        adminPermissions(admin)
        custPermissions(custRole)
        readOnlyPermissions(readonly)
        SecRole.repo.flush()
    }

    static void readOnlyPermissions(SecRole role) {
        role.permissions = ["rally:*:read"]
        role.persist()
    }

    static void adminPermissions(SecRole role){

        role.permissions = [ "*:*:*",
          'rally:org:*',
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
        ]

        role.persist()
    }

    static void custPermissions(SecRole role){
        role.permissions = ['rally:org:list,get,post',
         // 'rally:contact:*',
         'rally:activity:list,get',
         'rally:attachment:*',
         'rally:tag:list,get'
        ]
        role.persist()
    }

}
