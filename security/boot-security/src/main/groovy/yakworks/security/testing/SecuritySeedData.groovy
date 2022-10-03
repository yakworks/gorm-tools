/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.testing

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

import grails.gorm.transactions.Transactional
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRolePermission

@SuppressWarnings('BuilderMethodWithSideEffects')
@CompileStatic
class SecuritySeedData {

    @Autowired JdbcTemplate jdbcTemplate

    void fullMonty(){
        createRoles()
        createAppUsers()
    }

    @Transactional
    void createAppUsers(){

        AppUser admin = new AppUser([
            id: (Long)1, username: "admin", email: "admin@9ci.com", password:"123", orgId: 2
        ]).persist()

        admin.addRole('ADMIN', true)
        admin.addRole('POWER_USER', true)

        AppUser custUser = new AppUser([
            id: (Long)2, username: "cust", email: "cust@9ci.com", password:"123", orgId: 2
        ]).persist()
        assert custUser.id == 2

        admin.addRole('CUSTOMER', true)

        AppUser noRoleUser = AppUser.create([id: 3L, username: "noroles", email: "noroles@9ci.com", password:"123", orgId: 3], bindId: true)
        assert noRoleUser.id == 3
    }

    @Transactional
    void createRoles(){

        SecRole admin = new SecRole(id: (Long)1, code: SecRole.ADMIN).persist()
        SecRole power = new SecRole(id: (Long)2, code: "POWER_USER").persist()
        SecRole custRole = new SecRole(id: (Long)3, code: "CUSTOMER").persist()

        //add permissions
        adminPermissions(admin)
        custPermissions(custRole)
    }

    void adminPermissions(SecRole role){

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
