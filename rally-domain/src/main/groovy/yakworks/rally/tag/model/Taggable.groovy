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
trait Taggable {

    List<Tag> getTags(){
        TagLink.repo.listTags(this as Persistable)
    }

    static Map constraintsMap = [
        tags: [ d: 'the tags for this item', validate: false] //validate false so it does not initialize
    ]
}
