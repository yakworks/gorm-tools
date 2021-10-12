/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.repo

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import gorm.tools.repository.model.AbstractLinkedEntityRepo
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink

@GormRepository
@CompileStatic
class ActivityLinkRepo extends AbstractLinkedEntityRepo<ActivityLink, Activity> {

    ActivityLinkRepo(){
        super(Activity, 'activity')
    }

    /**
     * iterates through and removes all by Activity vs doing deleteAll so that delete event is fired
     */
    void remove(Activity act, Map args = [:]) {
        for( ActivityLink link : list(act)){
            doRemove(link, args)
        }
    }

}
