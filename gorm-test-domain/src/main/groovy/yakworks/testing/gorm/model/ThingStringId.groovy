/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.model

import gorm.tools.model.NameCode
import gorm.tools.repository.model.PersistableRepoEntity
import grails.persistence.Entity

@Entity
class ThingStringId implements NameCode<ThingStringId>, PersistableRepoEntity<ThingStringId, String> {

    //Need to have explicit String id field for class to compile.
    //Otherwise, grails AST will add Long field by default, Persistable also requires getId() to match the generic type for ID
    //Keeping ID field as alias to code also makes it possible to run ID based queries.
    String id

    static mapping = {
        id generator: 'assigned', name: 'code', unique: true
    }

}
