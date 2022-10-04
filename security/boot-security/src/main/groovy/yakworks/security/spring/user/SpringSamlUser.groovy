/*
* Copyright 2006-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.user


import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal
import org.springframework.util.Assert

import yakworks.security.user.BasicUserInfo
import yakworks.security.user.UserInfo

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
@InheritConstructors
@CompileStatic
class SpringSamlUser extends DefaultSaml2AuthenticatedPrincipal implements SpringUserInfo {
    private static final long serialVersionUID = 1

    // SpringSamlUser(String name, Map<String, List<Object>> attributes,  List<String> sessionIndexes) {
    //     super(name, attributes, sessionIndexes)
    // }

    @Override
    Collection<? extends GrantedAuthority> getAuthorities(){
        return SpringUserUtils.rolesToAuthorities(roles)
    }

    static SpringSamlUser of(Saml2AuthenticatedPrincipal samlAuthPrincipal){
        return SpringSamlUser.of(samlAuthPrincipal, [] as List<String>)
    }

    static SpringSamlUser of(Saml2AuthenticatedPrincipal samlAuthPrincipal, Collection<String> roles){
        def spu = new SpringSamlUser(samlAuthPrincipal.name, samlAuthPrincipal.attributes, samlAuthPrincipal.sessionIndexes)
        spu.relyingPartyRegistrationId = samlAuthPrincipal.relyingPartyRegistrationId
        spu.roles = roles as Set<String>
        return spu
    }


    @Override
    String getPassword() {
        return null
    }

    @Override
    String getUsername() {
        return null
    }

    @Override
    boolean isAccountNonExpired() {
        return false
    }

    @Override
    boolean isAccountNonLocked() {
        return false
    }

    @Override
    boolean isCredentialsNonExpired() {
        return false
    }

    @Override
    boolean isEnabled() {
        return false
    }

}
