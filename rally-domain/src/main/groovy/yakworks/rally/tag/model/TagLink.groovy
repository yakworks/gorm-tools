/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.model


import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.tag.repo.TagLinkRepo

/**
 * generalized composite table to link a Tag to any entity
 */
@Entity
@GrailsCompileStatic
class TagLink implements TagLinkTrait<TagLink, TagLinkRepo>, Serializable {
    static belongsTo = [tag: Tag]
    String linkedEntity
    Long linkedId

    static mapping = {
        id composite: ['linkedId', 'linkedEntity', 'tag']
        version false
        tag column: 'tagId', fetch: 'join'
    }

    static constraints = {
        linkedEntity nullable: false, blank: false
        linkedId nullable: false
    }

    static List<TagLink> listByTag(Tag tag) {
        getRepo().listByTag(tag)
    }

    static void removeAllByTag(Tag tag) {
        getRepo().removeAllByTag(tag)
    }

    static boolean exists(Tag tag) {
        getRepo().exists(tag)
    }

}
