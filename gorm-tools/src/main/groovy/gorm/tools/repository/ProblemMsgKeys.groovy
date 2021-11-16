/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import groovy.transform.CompileStatic

import yakworks.i18n.MsgKey

/**
 * A bunch of statics to support the Repository artifacts.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class ProblemMsgKeys {

    static MsgKey validationError(Object entity) {
        validationError(entity.class.simpleName)
    }

    static MsgKey validationError(String entityName) {
        MsgKey.of('error.validation', [entityName: entityName])
    }

    static MsgKey notSaved(Object entity) {
        MsgKey.of('error.persist', [entityName: entity.class.simpleName])
    }

    static MsgKey notFoundId(Class entityClass, Serializable id) {
        MsgKey.of('error.notFound', [entityName: entityClass.simpleName, id: id])
    }

    // static MsgKey notDeleted(Object entity, Serializable ident) {
    //     String entityName = entity.class.simpleName
    //     return new SpringMsgKey('default.not.deleted.message', [entityName, ident], "${entityName} with id ${ident} could not be deleted")
    // }

    static MsgKey optimisticLockingFailure(Object entity) {
        MsgKey.of('error.optimisticLocking', [entityName: entity.class.simpleName])
    }

}
