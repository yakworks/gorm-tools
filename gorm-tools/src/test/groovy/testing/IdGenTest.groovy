/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import gorm.tools.repository.RepoEntity
import gorm.tools.repository.api.GetRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.api.IdGeneratorRepo
import gorm.tools.transform.IdEqualsHashCode
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/**
 * tests the IdGeneratorRepo
 */
@IdEqualsHashCode
@Entity @RepoEntity
@GrailsCompileStatic
class IdGenTest implements GetRepo<IdGenTestRepo> { //, GetRepo<IdGenTestRepo> {
    String name

    static mapping = {
        id generator:'assigned'
    }

    static constraints = {
        name nullable: false
    }

    String someFoo(){ getRepo().someFoo()}
}

@GormRepository
class IdGenTestRepo implements GormRepo<IdGenTest>, IdGeneratorRepo {

    String someFoo(){ return 'bar'}
}
