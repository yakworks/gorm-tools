/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.repo

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.model.Persistable
import gorm.tools.repository.events.RepositoryEvent

/**
 * Basic helpers to keep
 */
@CompileStatic
trait TaggableRepoSupport {

    @Autowired(required = false)
    TagLinkRepo tagLinkRepo


    // call in beforeRemove
    void removeTagLinks(Persistable linkedEntity) {
        tagLinkRepo.remove(linkedEntity)
    }

    // call in afterPersist
    void addOrRemoveTags(Persistable linkedEntity, Object itemParams) {
        tagLinkRepo.addOrRemove(linkedEntity, itemParams)
    }

    // call in afterPersist
    void addOrRemoveTags(Persistable linkedEntity, RepositoryEvent e) {
        if (e.bindAction && e.data?.tags){
            tagLinkRepo.addOrRemove(linkedEntity, e.data.tags)
        }
    }
}
