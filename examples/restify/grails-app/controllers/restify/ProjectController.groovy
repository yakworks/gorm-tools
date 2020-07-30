/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import javax.annotation.PostConstruct

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovyx.gpars.util.PoolUtils

import org.springframework.util.ClassUtils

import gorm.tools.rest.controller.RestApiRepoController
import grails.core.GrailsApplication
import grails.util.GrailsNameUtils
import taskify.Project

import static org.springframework.http.HttpStatus.CREATED

@CompileStatic
class ProjectController extends RestApiRepoController<Project> {

    ProjectController() {
        super(Project, false)
    }

    @Override
    def post() {
        try {
            Map q = getDataMap()
            String comments = q.comments ?: ""
            q.comments = "$comments - post was here"
            Project instance = getRepo().create(q)
            respond instance, [status: CREATED] //201
        } catch (RuntimeException e) {
            handleException(e)
        }
    }

}
