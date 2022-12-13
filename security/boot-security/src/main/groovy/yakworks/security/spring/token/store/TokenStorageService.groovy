/*
* Copyright 2013-2016 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.spring.token.store

import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails

import yakworks.security.spring.token.store.TokenNotFoundException

/**
 * Implementations of this interface are responsible to load user information from a token storage system, and to store
 * token information into it.
 */
interface TokenStorageService {

    /**
     * Returns a principal object given the passed token value
     * @throws yakworks.security.spring.token.store.TokenNotFoundException if no token is found in the storage
     */
    UserDetails loadUserByToken(String tokenValue) throws TokenNotFoundException

    /**
     * Stores a token. It receives the principal to store any additional information together with the token,
     * like the username associated.
     *
     * @see Authentication#getPrincipal()
     */
    void storeToken(String tokenValue, UserDetails principal)

    /**
     * Removes a token from the storage.
     * @throws TokenNotFoundException if the given token is not found in the storage
     */
    void removeToken(String tokenValue) throws TokenNotFoundException
}
