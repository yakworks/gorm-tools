/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import javax.persistence.Transient

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import org.springframework.lang.Nullable

@Entity
@GrailsCompileStatic
class CompositeNoVersion implements RepoEntity<CompositeNoVersion>, Serializable {

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

    // @Override
    // void setId(@Nullable Serializable serializable) {
    //
    // }

    @Transient
    boolean isNew(){
        return !isAttached()
    }
}
