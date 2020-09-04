/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.taskify.domain

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileDynamic

import gorm.tools.AuditStamp
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.taskify.domain.traits.NameNumConstraints
import yakworks.taskify.domain.traits.NameNumTrait

@AuditStamp
@Entity
@GrailsCompileStatic
class Org implements NameNumTrait{
    //strings
    String name2
    String secret
    //boolean
    Boolean inactive = false
    //decimal
    BigDecimal revenue
    BigDecimal creditLimit //used for gtef compares
    //dates
    Date actDate
    LocalDate locDate
    LocalDateTime locDateTime
    //special
    //Currency currency //FIXME creates and overflow
    Org link
    //Associations
    OrgType type //type is required
    Location location //belongs to whatever

    // since OrgExt also has an Org property (orgParent) it gets confused and
    // needs to know that its "belongs" to is the map and that orgParent gets set sepearatly
    OrgExt ext //<- ext belong to org
    static mappedBy = [ext: "org"]

    //used for event testing
    String event
    String stampEvent

    //enums
    Kind kind
    OrgStatus status

    @CompileDynamic //bug in grailsCompileStatic requires this on internal enums
    enum Kind {CLIENT, VENDOR, PARENT}

    static mapping = {
        //id generator:'assigned'
        ext column: 'extId'
        status enumType: 'identity'
    }

    static constraints = {
        importFrom(NameNumConstraints)

        name2    nullable: true
        secret   nullable: true, display: false

        inactive nullable: true, required: false
        revenue  nullable: true

        //dates
        actDate     nullable: true, example: "2018-01-26T01:36:02Z"
        locDate     nullable: true, example: "2018-01-25"
        locDateTime nullable: true, example: "2018-01-01T01:01:01"
        //special
        //currency    nullable: true

        //enums
        kind nullable: false
        status nullable: true

        //Associations
        type nullable: false
        location nullable: true
        ext  nullable: true
        link bindable: true
    }

    // gorm event
    // def beforeValidate() {
    //     if (ext && !ext.id) ext.org = this
    // }

    static config = """{
        json {
            includes = '*' //should be default
            excludes = ['location']
        }
        query.quickSearch = ["name", "name2"]
        audittrail.enabled = false
        autotest.update = [name:'foo']
    }"""

}
