/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository


import groovy.transform.CompileStatic

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.support.RequestContextUtils

import gorm.tools.support.MsgKey
import gorm.tools.support.MsgSourceResolvable
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
        return new MsgKey("validation.error", [entityName], "$entityName validation errors")
    }

    static MsgSourceResolvable validationError(String entityName) {
        return new MsgKey("validation.error", [entityName], "$entityName validation errors")
    }

    static MsgSourceResolvable notSaved(Object entity) {
        String entityName = entity.class.simpleName
        return new MsgKey("persist.error", [entityName], "$entityName save failed")
    }

    static MsgSourceResolvable notFoundId(Class entityClass, Serializable id) {
        String entityName = entityClass.simpleName
        return new MsgKey("default.not.found.message", [entityName, id], "${entityName} not found for id:${id}")
    }

    // @CompileDynamic
    // static Map buildLightMessageParams(Object entity) {
    //     String ident = entity.id
    //     String domainLabel = entity.class.name
    //     List args = [domainLabel, ident]
    //     return [ident: ident, domainLabel: domainLabel, args: args]
    // }

    static MsgSourceResolvable notDeleted(Object entity, Serializable ident) {
        String entityName = entity.class.simpleName
        return new MsgKey('default.not.deleted.message', [entityName, ident], "${entityName} with id ${ident} could not be deleted")
    }

    static MsgSourceResolvable optimisticLockingFailure(Object entity) {
        String entityName = entity.class.simpleName
        return new MsgKey("default.optimistic.locking.failure", [entityName], "Another user has updated the ${entityName} while you were editing")
    }

    // static String resolveDomainLabel(Object entity) {
    //     return resolveMessage("${propName(entity.class.name)}.label", "${GrailsNameUtils.getShortName(entity.class.name)}")
    // }
    // @Deprecated
    // static Locale defaultLocale() {
    //     // try {
    //     //     GrailsWebRequest webRequest = RequestContextHolder.currentRequestAttributes() as GrailsWebRequest
    //     //     Locale currentLocale = RequestContextUtils.getLocale(webRequest.getCurrentRequest())
    //     //     return currentLocale
    //     // }
    //     // catch (java.lang.IllegalStateException e) {
    //     //     return Locale.ENGLISH
    //     // }
    //     return LocaleContextHolder.getLocale()
    // }

    static String propName(String prop) {
        NameUtils.getPropertyName(prop)
    }

    //used for messages, if the entity has a name field then use that other wise fall back on the id and return that
    // @CompileDynamic
    // static String badge(Serializable id, GormEntity entity) {
    //     boolean hasName = entity?.metaClass.hasProperty(entity, 'name')
    //     return ((hasName && entity) ? entity.name : id)
    // }

}
