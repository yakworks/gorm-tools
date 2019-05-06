/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.grails.datastore.gorm.GormEntity

/**
 * Delegates missing properties as method calls to the repository for the domain class.
 */
@CompileStatic
class RepoDelegatingBean extends DelegatingBean {
    RepositoryApi repo

    @CompileStatic(TypeCheckingMode.SKIP)
    RepoDelegatingBean(GormEntity target) {
        super(target)
        repo = target.getRepo()
    }

    //first try if target bean has property, if not, check if repository has the method
    Object propertyMissing(String name) {
        try {
            return super.propertyMissing(name)
        } catch (MissingPropertyException e) {
            String method
            if (name.startsWith("has") || name.startsWith("is")) {
                method = name
            } else {
                method = "get" + name.capitalize()
            }

            try {
                return repo.invokeMethod(method, target)
            } catch (MissingMethodException me) {
                //repository does not have that method either, so throw back original MissingPropertyException exception
                throw e
            }
        }
    }

    Object methodMissing(String name, args) {
        try {
            return target.invokeMethod(name, args)
        } catch (MissingMethodException e) {
            try {
                repo.invokeMethod(name, args)
            } catch (MissingMethodException me) {
                //if repository does not have the method either, throw back original exception
                throw e
            }
        }
    }

}
