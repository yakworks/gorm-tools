/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.model

import gorm.tools.model.NameCode
import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.StringIdRepoEntity
import grails.persistence.Entity

@Entity
class ThingStringId implements NameCode<ThingStringId>, StringIdRepoEntity<ThingStringId, GormRepo> {
    String id

    static mapping = {
        id generator: 'assigned', name: 'code', unique: true
    }
    
}
