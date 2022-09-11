/*
* Copyright 2006-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring

import groovy.transform.CompileStatic

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

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
class SpringSecUser extends User {

    private static final long serialVersionUID = 1

    /** the database id from the stored User */
    final Serializable id

    /** the organization ID */
    Long orgId

    /**
     * Constructor.
     *
     * @param username the username presented to the
     *        <code>DaoAuthenticationProvider</code>
     * @param password the password that should be presented to the
     *        <code>DaoAuthenticationProvider</code>
     * @param enabled set to <code>true</code> if the user is enabled
     * @param accountNonExpired set to <code>true</code> if the account has not expired
     * @param credentialsNonExpired set to <code>true</code> if the credentials have not expired
     * @param accountNonLocked set to <code>true</code> if the account is not locked
     * @param authorities the authorities that should be granted to the caller if they
     *        presented the correct username and password and the user is enabled. Not null.
     * @param id the id of the domain class instance used to populate this
     */
    SpringSecUser(String username, String password, boolean enabled, boolean accountNonExpired,
                  boolean credentialsNonExpired, boolean accountNonLocked,
                  Collection<GrantedAuthority> authorities, Serializable id) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired,
            accountNonLocked, authorities)
        this.id = id

    }

    SpringSecUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Serializable id) {
        super(username, password, true, true, true, true, authorities)
        this.id = id
    }
}
