/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import java.time.LocalDate

import gorm.tools.model.NameDescription
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Book implements NameDescription, RepoEntity<Book> {

    BigDecimal cost
    LocalDate publishDate
    String comments

    //default includes fields for json render with EntityMap
    static List includes = ['id', 'name', 'description', 'publishDate']

}
