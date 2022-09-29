/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEnhancer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.GenericTypeResolver

import yakworks.security.user.CurrentUser
import yakworks.security.user.UserInfo

/**
 * common generic helpers for security, implement with generics D for the domain entity and I for the id type
 */
@CompileStatic
trait SecService<D> {

    Class<D> entityClass

    Class<D> getEntityClass() {
        if (!entityClass) this.entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), SecService)
        return entityClass
    }

    @Autowired(required = false)
    CurrentUser currentUser

    /**
     * encodes the password
     */
    abstract String encodePassword(String password)

    /**
     * Used in automation to username a bot/system user, also used for tests
     */
    abstract void loginAsSystemUser()
    abstract void reauthenticate(String username, String password)

    /**
     * get the user entity for the id. Default impl is to pull from DB.
     * @param uid the user id
     * @return the user entity
     */
    UserInfo getUser(Serializable uid) {
        GormEnhancer.findStaticApi(getEntityClass()).get(uid) as UserInfo
    }

}
