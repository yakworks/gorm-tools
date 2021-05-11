/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model


import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.orgs.repo.OrgTagRepo
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLinkTrait

@Entity
@GrailsCompileStatic
class OrgTag implements TagLinkTrait<OrgTag, OrgTagRepo>, Serializable {
    static transients = ['linkedEntity']
    static belongsTo = [tag: Tag]
    Long linkedId
    String linkedEntity = 'Org'

    static mapping = {
        version false
        id composite: ['linkedId', 'tag']
        tag column: 'tagId', fetch: 'join'
        linkedId column: 'orgId'
    }

    static constraints = {
        linkedId nullable: false
        tag nullable: false
    }

    static constraintsMap = [
        linkedId:[ description: 'the id of the entity this tag is linked to', nullable: false],
        tag:[ description: 'The tag', nullable: false]
    ]

}
