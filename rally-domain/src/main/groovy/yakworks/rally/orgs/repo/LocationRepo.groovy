/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener
import yakworks.rally.orgs.model.Location

@GormRepository
@Slf4j
@CompileStatic
class LocationRepo implements GormRepo<Location> {

    @RepoListener
    void beforeValidate(Location location) {
        if(!location.org && location.contact) location.org = location.contact.org
    }

}
