/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import org.apache.shiro.SecurityUtils
import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ThreadContext

import grails.artefact.Interceptor

@CompileStatic
class ShiroInterceptor implements Interceptor {

    ShiroInterceptor() {
        matchAll()
    }

    boolean before() {
        //if its nested then we pass in rootResource so use that, remove it so it doesnt get passed to criteria
        String nspace = controllerNamespace
        String rootResource = controllerName
        String act = actionName
        println "params $params"
        //if its nested then we pass in rootResource so use that, remove it so it doesnt get passed to criteria
        String rootResourceVal = params.remove('rootResource')
        if(rootResourceVal) rootResource = rootResourceVal
        println "params after remove $params"

        // if not authenticated then let it flow and blow 403. Also, only restricting by namespace
        if(!ThreadContext.getSubject() || !controllerNamespace) {
            // println " -- no controllerNamespace params $params"
            // println " -- ThreadContext.getSubject() ${ThreadContext.getSubject()}"
            // println " -- controllerNamespace ${controllerNamespace}"
            return true
        }

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
