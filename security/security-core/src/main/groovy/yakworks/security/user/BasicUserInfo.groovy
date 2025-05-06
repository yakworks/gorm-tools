/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.user

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import yakworks.commons.model.Named

/**
 * Default implementation of UserInfo, used in testing and as a DTO to setup a user.
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString
@CompileStatic
class BasicUserInfo implements Named, UserInfo {
    Serializable id
    String  username
    String  displayName
    String  name
    String  email
    String  passwordHash
    boolean enabled = true
    Long orgId
    Set roles = [] as Set
    Set permissions = [] as Set
    Map<String, Object> attributes = [:] as Map<String, Object>

    static BasicUserInfo of(String username){
        return new BasicUserInfo(username: username)
    }

    static BasicUserInfo of(String username, Collection<String> roles){
        return new BasicUserInfo(username: username, roles: roles as Set)
    }

    static BasicUserInfo create(Map<String, Object> props){
        return new BasicUserInfo(props)
    }
}
