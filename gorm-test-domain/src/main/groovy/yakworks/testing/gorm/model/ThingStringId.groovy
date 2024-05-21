/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.model

import gorm.tools.mango.api.QueryMangoEntity
import gorm.tools.model.NameCode
import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.PersistableRepoEntity
import grails.persistence.Entity

@Entity
class ThingStringId implements NameCode<ThingStringId>, PersistableRepoEntity<ThingStringId, GormRepo, String>, QueryMangoEntity<ThingStringId> {
    String id

    static mapping = {
        id generator: 'assigned'
        code column: 'id', insertable: false, updateable: false
    }

    //need to set code from id, code is nullable:false
    void beforeValidate() {
        if(!this.code && this.id) this.code = this.id
        if(!this.name && this.id) this.name = id.replaceAll('-', ' ')
    }
}
