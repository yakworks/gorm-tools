/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.auditable.resolvers

import groovy.transform.CompileStatic

import org.grails.web.servlet.mvc.GrailsWebRequest

import grails.plugins.orm.auditable.AuditLogContext

@CompileStatic
class DefaultAuditRequestResolver implements AuditRequestResolver {
    @Override
    String getCurrentActor() {
        GrailsWebRequest request = GrailsWebRequest.lookup()
        request?.remoteUser ?: request?.userPrincipal?.name ?: AuditLogContext.context.defaultActor ?: 'N/A'
    }

    @Override
    String getCurrentURI() {
        GrailsWebRequest request = GrailsWebRequest.lookup()
        request?.request?.requestURI
    }
}
