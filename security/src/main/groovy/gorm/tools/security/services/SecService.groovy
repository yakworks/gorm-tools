/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.services

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEnhancer
import org.springframework.core.GenericTypeResolver

/**
 * common generic helpers for security, implement with generics D for the domain entity and I for the id type
 */
@CompileStatic
trait SecService<D> {

    /**
     * The java class for the Gorm domain (persistence entity) that is the user
     */
    Class<D> entityClass // the domain class this is for

    /**
     * The gorm domain class. uses the {@link org.springframework.core.GenericTypeResolver} is not set during contruction
     */
    Class<D> getEntityClass() {
        if (!entityClass) this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), SecService)
        return entityClass
    }

    /**
     * is a user logged in
     */
    abstract boolean isLoggedIn()

    /**
     * Gets the currently logged in user id
     */
    abstract Serializable getUserId()

    /**
     * encodes the password
     */
    abstract String encodePassword(String password)

    /**
     * Used in automation to username a bot/system user, also used for tests
     */
    abstract void loginAsSystemUser()

    /**
     * Get the domain class instance associated with the current authentication.
     */
    D getUser() {
        if (!isLoggedIn()) {
            return null
        }
        getUser(getUserId())
    }

    /**
     * returns the user display name for the passed in id
     * @return the user name
     */
    String getUserName(Serializable uid) {
        D usr = getUser(uid)
        return usr ? usr['name'] : null
    }

    /**
     * get the user entity for the id
     * @param uid the user id
     * @return the user entity
     */
    D getUser(Serializable uid) {
        GormEnhancer.findStaticApi(getEntityClass()).load(uid)
    }

}
