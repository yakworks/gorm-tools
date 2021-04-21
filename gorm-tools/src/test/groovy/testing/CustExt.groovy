/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class CustExt implements RepoEntity<CustExt> {
    static belongsTo = [org:Cust]

    String text1
    String text2

    static mapping = {
        id generator:'foreign', params:[property:'org']
        org insertable: false, updateable: false , column:'id'
    }
    static constraints = {
        text1 nullable: false
        text2 nullable: true
    }
}
