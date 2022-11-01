/*
* Copyright 2006-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.saml

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal

import yakworks.security.spring.user.SpringUserInfo

/**
 * POC NOT USED
 * This extends Saml2AuthenticatedPrincipal and implements UserInfo and UserDetails.
 * Giving it a compatible interface with all the bases.
 */
@SuppressWarnings(['ParameterCount'])
@InheritConstructors
@CompileStatic
class SpringSamlUser extends DefaultSaml2AuthenticatedPrincipal implements SpringUserInfo {
    private static final long serialVersionUID = 1

    /** Hard wire username */
    String username

    /** This is also a AuthenticatedPrincipal so name is used. Override it completely to take ambiguity out of it */
    String name

    SpringSamlUser(Saml2AuthenticatedPrincipal samlAuthPrincipal, SpringUserInfo userInfo){
        super(samlAuthPrincipal.name, samlAuthPrincipal.attributes, samlAuthPrincipal.sessionIndexes)
        this.relyingPartyRegistrationId = samlAuthPrincipal.relyingPartyRegistrationId
        //keep userName  from okta saml
        // attributes['userName'] = samlAuthPrincipal.name
        setUsername(userInfo.username)
        merge(userInfo)
        roles = userInfo.roles
        // attributesToUserProfile()
    }
    /**
     * Build SpringSamlUser from the built SamlAuthPricipal (from Okta) and merge in the userInfo from internal db
     * @param samlAuthPrincipal SamlAuthPricipal (built from Okta)
     * @param userInfo our userDetails from database, built from AppUser
     * @return the new intance to store in authentication
     */
    static SpringSamlUser of(Saml2AuthenticatedPrincipal samlAuthPrincipal, SpringUserInfo userInfo){
        //name doesnt matter here as it gets set in merge
        def spu = new SpringSamlUser(samlAuthPrincipal, userInfo)
        return spu
    }

    /** Saml spec is for attributes to be a list, which 99.9% of the time its not. So convert its attributes to our userProfile map*/
    // def attributesToUserProfile(){
    //     getAttributes().each { k, v ->
    //         userProfile[k] = v[0] //first item in list
    //     }
    // }

    @Override
    String getPassword() {
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
    String getPasswordHash() {
        return "N/A"
    }

    @Override
    boolean isEnabled() {
        return true
    }

}
