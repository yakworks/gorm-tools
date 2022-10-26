/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.user

import groovy.transform.CompileStatic

import yakworks.commons.model.Named

/**
 * Default implementation of UserInfo, used in testing and as a DTO to setup a user.
 */
@CompileStatic
trait UserInfoTrait implements Named, UserInfo {
    // Serializable id
    String  name
    String  username
    String  displayName
    String  email
    String  passwordHash
    boolean enabled = true
    Long orgId
    Set roles = [] as Set
    Set permissions = [] as Set
    Map<String, Object> attributes = [:] as Map<String, Object>

}
