/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository


import groovy.transform.CompileStatic

import gorm.tools.support.MsgSourceResolvable
import gorm.tools.support.SpringMsgKey
import yakworks.commons.lang.NameUtils

/**
 * A bunch of statics to support the Repository artifacts.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class RepoMessage {

    static MsgSourceResolvable validationError(Object entity) {
        String entityName = entity.class.simpleName
        return new SpringMsgKey("validation.error", [entityName], "$entityName validation errors")
    }

    static MsgSourceResolvable notSaved(Object entity) {
        String entityName = entity.class.simpleName
        return new SpringMsgKey("persist.error", [entityName], "$entityName save failed")
    }

}
