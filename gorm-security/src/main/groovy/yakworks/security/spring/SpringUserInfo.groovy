/*
* Copyright 2006-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

import yakworks.security.UserInfo

import static yakworks.security.spring.SpringUserInfoUtils.rolesToAuthorities

/**
 * Grails security has a GrailsUser that it uses by default, this replaces it to remove confusion.
 * NOTES:
 *  - Extends the default Spring Security User class (which implements the UserDetails interface)
 *  - adds the id (the default implementation will set to the AppUser.id)
 *  - We dont use the AppUser gorm domain and instead create this with the data from AppUser instance
 *  - think of it as a DTO or serializable value object for a Spring Security User, this is whats stored in the context for the logged in user
 *
 * @see org.springframework.security.core.userdetails.User
 */
@SuppressWarnings(['ParameterCount'])
@CompileStatic
class SpringUserInfo extends User implements UserInfo {
    private static final long serialVersionUID = 1

    // --- Implements UserInfo ---
    /** The unique id for the user, be default will be the unique generated id from db */
    protected Serializable id
    /** the username is implemented in the User object */

    /** the full name, may come from contact or defaults to username if not populated */
    protected String name
    /** the display name */
    protected String displayName
    /** the users email */
    protected String email
    /** the organization ID */
    protected Serializable orgId

    Map<String, Object> userProfile

    /**
     * SpringUserInfo constructor
     * @param sourceUser the source UserInfo (AppUser)
     * @param password the password that should be presented to the
     *        <code>DaoAuthenticationProvider</code>
     * @param accountNonExpired set to <code>true</code> if the account has not expired
     * @param credentialsNonExpired set to <code>true</code> if the credentials have not expired
     * @param accountNonLocked set to <code>true</code> if the account is not locked
     */
    SpringUserInfo(UserInfo sourceUser,
                    String passwordHash,
                   boolean accountNonExpired,
                   boolean credentialsNonExpired,
                   boolean accountNonLocked,
                   Collection<? extends GrantedAuthority> authorities = null ) {

        super(sourceUser.username,
            passwordHash,
            sourceUser.enabled,
            accountNonExpired,
            credentialsNonExpired,
            accountNonLocked,
            authorities != null ? authorities : rolesToAuthorities(sourceUser.roles))

        copyUserInfo(sourceUser)
    }

    SpringUserInfo(UserInfo sourceUser, String password, Collection<? extends GrantedAuthority> authorities = null) {
        super(sourceUser.username, password, authorities != null ? authorities : rolesToAuthorities(sourceUser.roles))
        copyUserInfo(sourceUser)
    }

    SpringUserInfo(UserInfo sourceUser) {
        super(sourceUser.username, sourceUser.passwordHash, rolesToAuthorities(sourceUser.roles))
        copyUserInfo(sourceUser)
    }

    /** minumum for mocking our a user */
    SpringUserInfo(String username, String password, Collection<? extends GrantedAuthority> authorities,
                    Serializable id, Serializable orgId) {
        super(username, password, authorities)

    }

    void copyUserInfo(UserInfo sourceUser){
        this.id = sourceUser.id
        this.name = sourceUser.name
        this.displayName = sourceUser.displayName
        this.email = sourceUser.email
        this.orgId = sourceUser.orgId
    }

    @Override //UserInfo
    Serializable getId() {
        return this.id
    }

    @Override
    String getPasswordHash() {
        return this.password
    }

    @Override //UserInfo
    String getName() {
        return this.name
    }

    @Override //UserInfo
    String getDisplayName() {
        return this.displayName
    }

    @Override //UserInfo
    String getEmail() {
        return this.email
    }

    @Override //UserInfo
    Serializable getOrgId() {
        return this.orgId
    }

    @Override //UserInfo
    Set<String> getRoles() {
        SpringUserInfoUtils.authoritiesToRoles(this.authorities);
    }
}
