/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.user

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

import yakworks.security.services.PasswordValidator
import yakworks.security.user.UserInfo

/**
 * Trait for UserInfo UserDetailsService
 * @see UserDetailsService
 */
@CompileStatic
trait UserInfoDetailsService implements UserDetailsService {
    // Logger LOG = LoggerFactory.getLogger(UserInfoDetailsService)
    @Autowired PasswordValidator passwordValidator

    // abstract SpringUserInfo loadUserByUserId(Serializable id)

    /**
     * Creates SpringUser (UserDetails) from the AppUser.
     * Throws UsernameNotFoundException is user is null
     *
     * @param user the user
     * @param message the suffix key for UsernameNotFoundException if user is null
     * @return the SpringUser
     */
    SpringUserInfo verifyAndCreateSpringUser(UserInfo user, String message){
        if (!user) throw new UsernameNotFoundException("User not found for $message")
        // LOG.debug "Found AppUser ${user.username}, creating SpringUser"
        SpringUser springUser = SpringUser.of(user)
        checkCredentialExpiration(springUser)
        return springUser
    }

    /**
     * sets the credentialsNonExpired=false (its backwards) if isPasswordExpired=true
     */
    @CompileDynamic //so we can set the private
    void checkCredentialExpiration(SpringUserInfo springUser){
        if(passwordValidator.isPasswordExpired(springUser.id) ){
            //its private so set with reflection
            ClassUtils.setFieldValue(springUser, "credentialsNonExpired", false )
        }
    }

}
