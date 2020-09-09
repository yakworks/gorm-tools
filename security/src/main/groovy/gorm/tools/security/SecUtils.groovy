/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import gorm.tools.beans.AppCtx
import gorm.tools.repository.api.RepositoryApi
import gorm.tools.security.services.SecService

class SecUtils {

    private static SecService cachedSecService

    private SecUtils() {
        // statics only
    }

    static String getUserName(userId) {
        return getSecService().getUserName(userId)
    }

    /**
     * gets the currently logged in user id
     */
    static Serializable getUserId() {
        getSecService().getUserId()
    }

    /**
     * gets the currently logged in user display name
     */
    static String getUserName() {
        getSecService().getUserName()
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
