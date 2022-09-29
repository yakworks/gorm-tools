/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.util.ReflectionUtils

import grails.gorm.transactions.Transactional
import yakworks.commons.lang.ClassUtils
import yakworks.security.gorm.AppUserService
import yakworks.security.gorm.PasswordValidator
import yakworks.security.gorm.model.AppUser

/**
 * Default Gorm-Tools implementation of UserDetailsService that uses AppUser to load users and roles
 * We dont use the default Gorm or GrailsUserDetailsService from Grails Spring Security because we need more flexibility for oauth and ldap.
 * This uses the AppUserService and creates baseline for the various OAuth and Ldap.
 * @see UserDetailsService
 */
@Slf4j
@CompileStatic
class AppUserDetailsService implements UserDetailsService {

    @Autowired PasswordValidator passwordValidator

    @Transactional
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug "loadUserByName(${username})"

        AppUser user = AppUser.getByUsername(username.trim())
        if (!user) {
            throw new UsernameNotFoundException("User not found: $username")
        }
        log.debug "Found user ${user} in the database"

        SpringUserInfo springUser = SpringUserInfo.of(user)
        checkCredentialExpiration(springUser, user)
        return springUser
    }

    /**
     * sets the credentialsNonExpired=false (its backwards) if isPasswordExpired=true
     */
    @CompileDynamic //so we can set the private
    void checkCredentialExpiration(SpringUserInfo springUser, AppUser sourceUser){
        if(passwordValidator.isPasswordExpired(sourceUser)){
            //its private so set with reflection
            ClassUtils.setFieldValue(springUser, "credentialsNonExpired", false )
        }
    }
}
