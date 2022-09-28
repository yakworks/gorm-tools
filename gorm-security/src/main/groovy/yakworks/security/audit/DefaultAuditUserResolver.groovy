/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.audit

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import yakworks.security.SecService
import yakworks.security.UserInfo

/**
 * Default implementation for AuditUserResolver
 * override and replace the bean if its desired to do anything special
 */
@CompileStatic
class DefaultAuditUserResolver implements AuditUserResolver {

    @Autowired SecService secService

    /**
     * @return the current actor
     */
    @Override
    Serializable getCurrentUserId(){
        Serializable uid = secService.getUserId()
        return uid ?: 0L
    }

    /**
     * get the UserInfo for the userId
     */
    UserInfo getUserInfo(Serializable userId){
        secService.getUser(userId)
    }
}
