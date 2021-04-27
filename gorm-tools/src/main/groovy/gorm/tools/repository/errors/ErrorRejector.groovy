/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import java.beans.Introspector

import groovy.transform.CompileStatic

/**
 * The rejector!
 * A helper trait for a repo to allow rejecting values for validation
 * based on how the org.springframework.validation.Errors works and
 * what is done in the org.grails.datastore.gorm.validation.constraints.AbstractConstraint
 *
 * why: the constraints in Gorm work fine but are messy and difficult to manage for anything but the out of the box
 * this allows us to rejectValues in the same way as the constraints do but tied to different config settings
 * and complicate business logic.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait ErrorRejector<D> {

    Set<String> buildRejectCodes(Object target, String propName, String code){
        def newCodes = [] as Set<String>
        Class targetClass = target.class
        String classShortName = Introspector.decapitalize(targetClass.getSimpleName())
        newCodes << "${targetClass.getName()}.${propName}.${code}".toString()
        newCodes << "${classShortName}.${propName}.${code}".toString()
        newCodes << "${code}.${propName}".toString()
        newCodes << code
    }


}
