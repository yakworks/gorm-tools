/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.model.Persistable
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.orgs.repo.OrgTagRepo
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLinkTrait

@Entity
@GrailsCompileStatic
class OrgTag implements TagLinkTrait<OrgTag>, GormRepoEntity<OrgTag, OrgTagRepo>, Serializable {

    static transients = ['linkedEntity']
    static belongsTo = [tag: Tag]

    //this is a transient and defaults
    String linkedEntity = 'Org'

    static mapping = {
        version false
        id composite: ['linkedId', 'tag']
        tag column: 'tagId', fetch: 'join'
        linkedId column: 'orgId'
    }


    static constraintsMap = [
        linkedEntity:[ description: 'dummy placeholder, will always be Org'],
    ]

    //Override
    static List<Tag> listTags(Persistable entity) {
        getRepo().listTags(entity)
    }
}
