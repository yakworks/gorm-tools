/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.RepoListener
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.common.LinkedEntityRepoTrait

@GormRepository
@Slf4j
@CompileStatic
class ActivityLinkRepo implements LinkedEntityRepoTrait<ActivityLink, Activity> {

    @Override
    String getItemPropName() {'activity'}

    @Override
    Activity loadItem(Long id) { Activity.load(id)}

    List<ActivityLink> listByActivity(Activity act) {
        query(activity: act).list()
    }

    void removeAllByActivity(Activity act) {
        listByActivity(act).each {
            it.remove()
        }
    }

    //We do this in afterBind, coz EntityMapBinder wont bind properties which are part of composite id
    // @RepoListener
    // void afterBind(ActivityLink activityLink, Map p, AfterBindEvent e) {
    //     if (p.linkedEntity) activityLink.linkedEntity = p.linkedEntity
    //     if (p.linkedId) activityLink.linkedId = p.linkedId as Long
    //     if (p.activity != null) {
    //         def activityParam = p.activity
    //         if (activityParam.getClass().isAssignableFrom(Activity)) activityLink.activity = (Activity) p.activity
    //         else if (activityParam instanceof Map) activityLink.activity = Activity.load(activityParam['id'] as Long)
    //         else {
    //             activityLink.activity = Activity.load(activityParam as Long)
    //         } //assume that its an id
    //     }
    // }

}
