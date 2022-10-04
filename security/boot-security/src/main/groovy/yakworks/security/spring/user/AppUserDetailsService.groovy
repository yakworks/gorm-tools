/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.user


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

import grails.gorm.transactions.Transactional
import yakworks.security.gorm.model.AppUser

/**
 * Default Gorm-Tools implementation of UserDetailsService that uses AppUser to load users and roles
 * We dont use the default Gorm or GrailsUserDetailsService from Grails Spring Security because we need more flexibility for oauth and ldap.
 * This uses the AppUserService and creates baseline for the various OAuth and Ldap.
 * @see UserDetailsService
 */
@Slf4j
@CompileStatic
class AppUserDetailsService implements UserInfoDetailsService {

    @Override
    @Transactional
    SpringUserInfo loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug "loadUserByName(${username})"
        AppUser user = AppUser.getByUsername(username.trim())
        return verifyAndCreateSpringUser(user, "username: $username")
    }

    @Override
    @Transactional
    SpringUserInfo loadUserByUserId(Serializable id) throws UsernameNotFoundException {
        log.debug "loadUserByUserId(${id})"
        AppUser user = AppUser.get(id)
        return verifyAndCreateSpringUser(user, "id: $id")
    }

}
