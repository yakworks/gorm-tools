/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.repo

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.common.LinkXRefRepo

@GormRepository
@CompileStatic
class ActivityLinkRepo implements LinkXRefRepo<ActivityLink, Activity> {

    @Override
    String getItemPropName() {'activity'}

    @Override
    Activity loadItem(Long id) { Activity.load(id)}

    List<ActivityLink> listByActivity(Activity act) {
        queryByItem(act).list()
    }

    void removeAllByActivity(Activity act) {
        listByActivity(act).each {
            it.remove()
        }
    }
}
