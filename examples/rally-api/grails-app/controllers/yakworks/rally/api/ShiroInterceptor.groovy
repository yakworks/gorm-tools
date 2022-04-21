/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import grails.artefact.Interceptor
import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ThreadContext

@CompileStatic
class ShiroInterceptor implements Interceptor {

    ShiroInterceptor() {
        matchAll()
    }

    boolean before() {
        // if not authenticated then let it flow and blow 403. Also, only restricting by namespace
        if(!ThreadContext.getSubject() || !controllerNamespace) return true
        String nspace = controllerNamespace
        String rootResource = controllerName
        String act = actionName
        //if its nested then we pass in rootResource so use that, remove it so it doesnt get passed to criteria
        if(params.rootResource) rootResource = params.remove('rootResource')

        String permission = "${nspace}:${rootResource}:${act}"
        // def reqAttrs = currentRequestAttributes()
        // println "reqAttrs ${reqAttrs}"
        Subject subject = SecurityUtils.getSubject()
        println "user=${subject.principal} permission=${permission}"
        //blows error if not authorized
        subject.checkPermission(permission)
        true
    }


    boolean after() {
        true
    }

    void afterView() {
        // no-op
    }
}
