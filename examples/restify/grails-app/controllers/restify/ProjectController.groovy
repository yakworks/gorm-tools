/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import groovy.transform.CompileStatic

import gorm.tools.rest.controller.RestApiRepoController
import taskify.Project

import static org.springframework.http.HttpStatus.CREATED

@CompileStatic
class ProjectController extends RestApiRepoController<Project> {

    ProjectController() {
        super(Project, false)
    }

    def post() {
        Map q = getDataMap()
        // println "q $q"
        String comments = q.comments ?: ""
        q.comments = "$comments - post was here"
        //q.num = q.num == null ? null : "foo"
        Project instance = getRepo().create(q)
        respond instance, [status: CREATED] //201
    }

    /**
     * GET /api/entity/${id}* update with params
     */
    def get() {
        try {
            // println "getter 2"
            respond getRepo().get(params)
        } catch (RuntimeException e) {
            handleException(e)
        }
    }

}
