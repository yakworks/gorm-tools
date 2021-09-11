/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.repo

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.model.Persistable

/**
 * Basic helpers to keep
 */
@CompileStatic
trait TaggableRepo {

    @Autowired(required = false)
    TagLinkRepo tagLinkRepo


    // call in beforeRemove
    void removeTagsLinks(Persistable linkedEntity) {
        tagLinkRepo.removeAll(linkedEntity)
    }

    // call in beforeRemove
    void bindTagsLinks(Persistable linkedEntity, Object itemParams) {
        tagLinkRepo.bind(linkedEntity, itemParams)
    }
}
