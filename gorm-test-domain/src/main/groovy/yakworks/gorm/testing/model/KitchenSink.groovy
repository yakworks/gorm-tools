/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileDynamic

import gorm.tools.audit.AuditStamp
import gorm.tools.hibernate.criteria.CreateCriteriaSupport
import gorm.tools.model.NameNum
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

/**
 * A sample domain model for ktichen sink testing
 */
@AuditStamp
@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class KitchenSink implements NameNum, RepoEntity<KitchenSink>, CreateCriteriaSupport {

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
    KitchenSink link
    //Associations
    Address location //belongs to whatever

    // since OrgExt also has an Org property (orgParent) it gets confused and
    // needs to know that its "belongs" to is the map and that orgParent gets set sepearatly
    KitchenSinkExt ext //<- ext belong to customer
    static mappedBy = [ext: "kitchenSink"]

    //used for event testing
    String event
    String beforeValidateCheck
    String stampEvent

    //enums
    Kind kind
    KitchenSinkStatus status

    //bug in grailsCompileStatic requires this on internal enums
    //also, internal enums must always come before the static constraints or it doesn't get set
    @CompileDynamic
    enum Kind {CLIENT, VENDOR, PARENT}

    static mapping = {
        //id generator:'assigned'
        ext column: 'extId' //, cascade: 'none'
        location column: 'locationId'
        status enumType: 'identity'
    }

    static constraintsMap = [
        secret: [ display: false ],
        inactive: [ required: false ],
        kind: [ nullable: false ],
        link: [ bindable: true ],
    ]

}
