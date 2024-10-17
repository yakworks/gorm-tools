/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.model.IdEnum

@Entity
@GrailsCompileStatic
class Cust implements RepoEntity<Cust> {
    String name
    UUID uid //an UUID field for testing
    Integer descId // inverted order for test
    //strings
    String name2
    String secret
    //boolean
    Boolean inactive = false
    //decimal
    BigDecimal amount
    BigDecimal amount2
    //dates
    Date date
    LocalDate locDate
    LocalDateTime locDateTime
    //special
    //FIXME creates an overflow, add back in and figure out why or if its still and issue
    //Currency currency

    //Associations
    CustType type //type is required
    Address location //belongs to whatever
    CustExt ext //<- ext belong to org

    //enums
    Kind kind = Kind.CLIENT
    TestIdent testIdent = TestIdent.Num2

    // event and repo testing
    String beforeValidateCheck

    static mapping = {
        //id generator:'assigned'
        testIdent enumType: 'identity'
    }

    static Map includes = [
        qSearch: ['name']
    ]

    static constraintsMap = [
        name:[      nullable: false],
        uid: [      nullable: true ],
        descId:[    nullable: true ],
        name2:[     nullable: true ],
        secret:[    nullable: true, display: false ],

        inactive:[  nullable: true, required: false ],
        amount:[    nullable: true ],
        amount2:[   nullable: true ],

        //dates
        date:[        nullable: true, example: "2018-01-26T01:36:02Z"],
        locDate:[     nullable: true, example: "2018-01-25"],
        locDateTime:[ nullable: true, example: "2018-01-01T01:01:01"],
        //special
        //currency    nullable: true

        //Associations
        type:[      nullable: false],
        location:[  nullable: true],
        ext:[       nullable: true],
    ]

    static CustRepo getRepo() { return (CustRepo) RepoLookup.findRepo(this) }

    @CompileDynamic //bug in grailsCompileStatic requires this on internal enums
    enum Kind {CLIENT, COMPANY}

    static boolean staticeRepoCall() {
        getRepo().sampleRepoCall()
    }

    boolean intstanceRepoCall() { getRepo().intstanceRepoCall(this) }
}

@CompileStatic
enum TestIdent implements IdEnum<TestIdent, Long> {
    Num2(2), Num4(4)
    final Long id

    TestIdent(Long id) { this.id = id }
}
