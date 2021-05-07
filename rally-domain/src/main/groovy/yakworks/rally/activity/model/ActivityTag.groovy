/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model


import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.activity.repo.ActivityTagRepo
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLinkTrait

@Entity
@GrailsCompileStatic
class ActivityTag implements TagLinkTrait<ActivityTag, ActivityTagRepo>, Serializable {
    static transients = ['linkedEntity']
    static belongsTo = [tag: Tag]
    Long linkedId
    String linkedEntity = 'Activity'

    static mapping = {
        version false
        id composite: ['linkedId', 'tag']
        tag column: 'tagId', fetch: 'join'
        linkedId column: 'activityId'
    }

    static constraints = {
        linkedId nullable: false
        tag nullable: false
    }

}
