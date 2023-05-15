/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import groovy.transform.CompileStatic

import yakworks.rest.gorm.controller.RestRepoApiController

import static org.springframework.http.HttpStatus.CREATED

@CompileStatic
class BookController implements RestRepoApiController<Book> {

    @Override
    def post() {
        try {
            Map q = bodyAsMap()
            String comments = q.comments ?: ""
            q.comments = "$comments - post was here"
            Book instance = getRepo().create(q)
            def entityMap = entityResponder.createEntityMap(instance, params)
            respondWith(entityMap, [status: CREATED])
            // respond instance, [status: CREATED] //201
        } catch (RuntimeException e) {
            handleException(e)
        }
    }

}
