/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import java.time.LocalDate

import gorm.tools.repository.RepoEntity
import grails.compiler.GrailsCompileStatic
import yakworks.taskify.domain.traits.NameDescriptionTrait

@RepoEntity
@GrailsCompileStatic
class Book implements NameDescriptionTrait {

    BigDecimal cost
    LocalDate publishDate

    //default includes fields for json render with EntityMap
    static List includes = ['id', 'name', 'description', 'publishDate']

    static constraints = {
        NameDescriptionTraitConstraints(delegate)
    }

}
