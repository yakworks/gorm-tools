/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testify.model

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileDynamic

import gorm.tools.audit.AuditStamp
import gorm.tools.hibernate.criteria.CreateCriteriaSupport
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.common.NameNum

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

    static constraints = {
        NameNumConstraints(delegate)

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
        location nullable: true
        ext  nullable: true
        link nullable: true, bindable: true
    }


    static config = """{
        json {
            includes = '*' //should be default
            excludes = ['location']
        }
        query.quickSearch = ["name", "name2"]
        audittrail.enabled = false
        autotest.update = [name:'foo']
    }"""

    // @Override
    // boolean equals(Object o) {
    //     if (o == null) return false
    //     if (this.is(o)) return true
    //     if (!(o instanceof Org)) return false
    //     Org other = (Org) o
    //     //if (id != null && other.id !=null) return id == other.id
    //     return id == other.id
    //     //return false
    // }



    // @Override
    // int hashCode() {
    //     int _result = HashCodeHelper.initHash();
    //     _result = HashCodeHelper.updateHash(_result, num)
    //     return HashCodeHelper.updateHash(_result, name)
    //     // if (this.getId() != this) {
    //     //     int var2 = HashCodeHelper.updateHash(_result, this.getId());
    //     //     _result = var2;
    //     // }
    //     //
    //     // return _result;
    //     // return new HashCodeBuilder()
    //     //     .append(num)
    //     //     .append(name)
    //     //     .toHashCode()
    // }

    // boolean equals(Object other) {
    //     if (other == null) {
    //         return false;
    //     } else if (this == other) {
    //         return true;
    //     } else if (!(other instanceof Org)) {
    //         return false;
    //     } else {
    //         Org otherTyped = (Org)other;
    //         return ScriptBytecodeAdapter.compareEqual(this.getId(), otherTyped.getId());
    //     }
    // }

    // @Override
    // int hashCode() {
    //     return 31 //when using id for equals then this is fine unless collection is large, like 10,000 items
    // }
}
