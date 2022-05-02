/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.LongIdGormRepo
import yakworks.rally.orgs.model.Location

@GormRepository
@CompileStatic
class LocationRepo extends LongIdGormRepo<Location> {

    @RepoListener
    void beforeValidate(Location location) {
        //if only contact is assign then fill org from that
        if(!location.orgId && location.contact) location.orgId = location.contact.orgId
    }

}
