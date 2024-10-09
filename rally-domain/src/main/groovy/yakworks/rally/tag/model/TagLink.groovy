/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.model

import gorm.tools.model.Persistable
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.tag.repo.TagLinkRepo

/**
 * generalized composite table to link a Tag to any entity
 */
@Entity
@GrailsCompileStatic
class TagLink implements TagLinkTrait<TagLink>, RepoEntity<TagLink>, Serializable {
    static belongsTo = [tag: Tag]

    static mapping = {
        id composite: ['linkedId', 'linkedEntity', 'tag']
        version false
        tag column: 'tagId', fetch: 'join' //fetch ensures the its populated in 1 query
    }

    static constraintsMap = [
        tag:[ description: 'the tag for the linked entity', validate: false ],
    ]

    static TagLinkRepo getRepo() { return (TagLinkRepo) RepoLookup.findRepo(this) }

    static List<TagLink> addTags(Persistable linkedEntity, List<Tag> tags) {
        getRepo().addTags(linkedEntity, tags)
    }


    /**
     * Add exists criteria to a DetachedCriteria if its has tags
     * in the criteriaMap
     */
    // static DetachedCriteria getExistsCriteria(Map criteriaMap, Class linkedEntityClazz, String linkedIdJoinProperty){
    //     getRepo().getExistsCriteria(criteriaMap, linkedEntityClazz, linkedIdJoinProperty)
    // }

}
