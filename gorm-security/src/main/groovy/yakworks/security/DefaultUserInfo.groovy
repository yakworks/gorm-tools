/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileStatic

import gorm.tools.model.NamedEntity

/**
 * Default implementation of UserInfo, used in testing and as a DTO to setup a user.
 */
@CompileStatic
class DefaultUserInfo implements NamedEntity, UserInfo {
    Serializable id
    String  username
    String  displayName
    String  name
    String  email
    String  passwordHash
    boolean enabled = true
    Long orgId
    Set roles = [] as Set
    Map userProfile

}
