/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.mapping


import groovy.transform.CompileStatic

import org.grails.datastore.gorm.validation.constraints.registry.ConstraintRegistry

@SuppressWarnings(['Indentation'])
@CompileStatic
class CrudUrlMappingsBuilder {
    String contextPath = '/api'
    String namespace
    String ctrl
    String parentResource
    Object builderDelegate

    CrudUrlMappingsBuilder(String namespace, String ctrl) {
        this.namespace = namespace
        this.ctrl = ctrl
    }

    CrudUrlMappingsBuilder(String namespace, String ctrl, Object builderDelegate) {
        this.namespace = namespace
        this.ctrl = ctrl
        this.parentResource = parentResource
        this.builderDelegate = builderDelegate
    }

    CrudUrlMappingsBuilder(String namespace, String ctrl, String parentResource, Object builderDelegate) {
        this.namespace = namespace
        this.ctrl = ctrl
        this.parentResource = parentResource
        this.builderDelegate = builderDelegate
    }

    static CrudUrlMappingsBuilder of(String rootPath, String namespace, String ctrl){
        def bldr = new CrudUrlMappingsBuilder(namespace, ctrl)
        bldr.contextPath = rootPath
        return bldr
    }

    CrudUrlMappingsBuilder withParent(String parentResource){
        this.parentResource = parentResource
        return this
    }

    /**
     * building url maps is pretty hacky in grails and there is no good clean way to do it without builder hacking.
     */
    void build(Object builderDelegate) {
        this.builderDelegate = builderDelegate
        build()
    }
    /**
     * building url maps is pretty hacky in grails and there is no good clean way to do it without builder hacking.
     */
    void build() {
        // String apiPathBase = namespace ? "${rootPath}/${namespace}".toString() : rootPath
        // Map params = parentResource ? [rootResource: parentResource] : [:]

        // GET /namespace/controller
        getBuilder('list').build()
        // GET /namespace/controller/$id
        getBuilder('get').withIdPattern().build()
        // GET /namespace/controller/picklist
        getBuilder('picklist').pattern('/picklist').build()
        // POST /namespace/controller
        getBuilder('post', 'POST').build()
        // POST /namespace/controller/$id
        getBuilder('put', 'PUT').withIdPattern().build()
        // PATCH /namespace/controller/$id
        getBuilder('put', 'PATCH').withIdPattern().build()
        // DELETE /namespace/controller/$id
        getBuilder('delete', 'DELETE').withIdPattern().build()

        //when a post is called allows an action
        // post "${apiPath}/$action(.$format)?"(controller: ctrl, namespace: namespace) {
        //     rootResource = rootResource
        // }
    }

    SimpleUrlMappingBuilder getBuilder(String action, String httpMethod = 'GET'){
        Map params = parentResource ? [rootResource: parentResource] : [:]

        def smb = SimpleUrlMappingBuilder.of(contextPath, namespace, ctrl).action(action)
            .httpMethod(httpMethod).parameters(params)
            .urlMappingBuilder(builderDelegate)
        return smb
    }


}
