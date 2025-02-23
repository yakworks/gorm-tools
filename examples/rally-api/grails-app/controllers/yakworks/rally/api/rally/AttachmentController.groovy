/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api.rally

import groovy.transform.CompileStatic

import yakworks.rally.attachment.model.Attachment
import yakworks.rest.gorm.controller.CrudApiController

@CompileStatic
class AttachmentController implements CrudApiController<Attachment> {

    static namespace = 'rally'

    def upload() {
        Map q = bodyAsMap()
        Map obj = [name: q.name]
        respondWith obj
    }

}
