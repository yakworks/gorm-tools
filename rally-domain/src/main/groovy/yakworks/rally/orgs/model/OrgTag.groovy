/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.gorm.DetachedCriteria
import grails.persistence.Entity
import yakworks.commons.beans.Transform
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

    /**
     * build exists criteria for the linkedId and tag list
     *
     */
    static DetachedCriteria buildExistsCriteria(List tagList, String linkedId = 'org_.id'){
        return OrgTag.query {
            eqProperty("linkedId", linkedId)
            inList('tag.id', Transform.toLongList(tagList))
        }.id()
    }

}
