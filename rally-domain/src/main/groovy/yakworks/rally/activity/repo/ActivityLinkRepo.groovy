/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.repo

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.AbstractLinkedEntityRepo
import grails.gorm.transactions.Transactional
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink

@GormRepository
@CompileStatic
class ActivityLinkRepo extends AbstractLinkedEntityRepo<ActivityLink, Activity> {

    ActivityLinkRepo(){
        super(Activity, 'activity')
    }

    /**
     * iterates through and removes all by Activity vs doing deleteAll so that delete event is fired.
     * Called while the activity itself is being deleted.
     */
    void remove(Activity act, Map args = [:]) {
        for( ActivityLink link : list(act)){
            doRemove(link, PersistArgs.of(args))
        }
    }

    /**
     * Removes the link.
     * When this is the only link, deletes the activity instead..
     */
    @Transactional
    void removeLink(Long activityId, String linkedEntity, Long linkedId) {
        Activity activity = Activity.get(activityId)
        RepoUtil.checkFound(activity, activityId, Activity.simpleName)

        ActivityLink link = query(activity: activity, linkedEntity: linkedEntity, linkedId: linkedId).get()

        //if this is last link, or there are no links, just remove the activity
        if (count(activity) <= 1) {
            activity.remove()
        } else if(link) {
            //remove single link
            doRemove(link, PersistArgs.defaults())
        }
    }

}
