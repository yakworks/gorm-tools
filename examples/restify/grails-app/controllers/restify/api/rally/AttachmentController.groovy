/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify.api.rally

import groovy.transform.CompileStatic

import gorm.tools.rest.controller.RestRepoApiController
import yakworks.rally.attachment.model.Attachment

@CompileStatic
class AttachmentController implements RestRepoApiController<Attachment> {

    static namespace = 'rally'

    def upload() {
        Map q = bodyAsMap()
        Map obj = [name: q.name]
        respond obj
    }

}
