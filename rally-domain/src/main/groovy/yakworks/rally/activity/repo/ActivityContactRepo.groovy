/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.repo

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import gorm.tools.repository.model.AbstractCrossRefRepo
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.orgs.model.Contact

@GormRepository
@CompileStatic
class ActivityContactRepo extends AbstractCrossRefRepo<ActivityContact, Activity, Contact> {

    ActivityContactRepo(){
        super(Activity, Contact, [ 'activity', 'contact'] )
    }

}
