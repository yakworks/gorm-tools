/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileStatic

import yakworks.spring.AppCtx

/**
 * a wrapper around SecService with statics for areas that can't get a bean injected such as in an Entity
 * DON'T USE THIS if injecting a SecService is an option
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1.12
 */
@CompileStatic
class SecUtils {

    boolean mapFoo(){
        []
    }

    private static SecService cachedSecService

    private SecUtils() {
        // statics only
    }

    static UserInfo getUser(Serializable uid) {
        return getSecService().getUser(uid)
    }

    static String getUserFullName(Serializable userId) {
        return getSecService().getUserFullName(userId)
    }

    static String getUserFullName() {
        getSecService().getUserFullName()
    }

    static String getUsername(Serializable userId) {
        return getSecService().getUsername(userId)
    }

    // static String getDisplayName(Serializable userId) {
    //     return getSecService().getDisplayName(userId)
    // }

    /**
     * gets the currently logged in username
     */
    static String getUsername() {
        getSecService().getUsername()
    }

    /**
     * gets the currently logged in user id
     */
    static Serializable getUserId() {
        getSecService().getUserId()
    }

    /**
     * finds the repo bean in the appctx if cachedRepo is null. returns the cachedRepo if its already set
     * @return The repository
     */
    static SecService getSecService() {
        if(!cachedSecService) cachedSecService = AppCtx.get('secService', SecService)
        return cachedSecService
    }

    /**
     * Used in automation to username a bot/system user, also used for tests
     */
    static void loginAsSystemUser() {
        getSecService().loginAsSystemUser()
    }

}
