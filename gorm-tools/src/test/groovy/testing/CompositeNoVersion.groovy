/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import javax.persistence.Transient

import gorm.tools.repository.model.CompositeRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class CompositeNoVersion implements CompositeRepoEntity<CompositeNoVersion>, Serializable {

    Long linkedId
    String name

    static mapping = {
        version false
        id composite: ['linkedId', 'name']
    }

    static constraints = {
        linkedId nullable: false
        name nullable: false
    }

    @Transient
    boolean isNew(){
        return !isAttached()
    }
}
