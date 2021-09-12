/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.model

import groovy.transform.CompileStatic

import gorm.tools.model.Persistable

/**
 * Marks a domain as taggable with a getTags() getter and constraintsMap
 */
@CompileStatic
trait Taggable implements HasTags {

    List<Tag> getTags(){
        TagLink.listTags(this as Persistable)
    }

    boolean hasTags() {
        return TagLink.hasTags((Persistable)this)
    }

    List<TagLink> addTags(List<Tag> tags) {
        TagLink.addTags((Persistable)this, tags)
    }

}
