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
import gorm.tools.repository.model.GormRepoEntity
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
class KitchenSink implements NameNum, GormRepoEntity<KitchenSink, KitchenSinkRepo>, CreateCriteriaSupport {

    //strings
    String name2
    String secret
    String comments

    //boolean
    Boolean inactive = false
    //decimal
    BigDecimal amount

    //dates
    Date actDate
    LocalDate localDate
    LocalDateTime localDateTime

    //self reference
    KitchenSink sinkLink

    //Associations
    Thing thing //belongs to whatever

    SinkExt ext
    //<- ext belong to KitchenSink
    // since ext also has an KitchenSink property (kitchenParent) it will confused
    // example of how to explcitly force the "belongsTo"  with the mappedBy
    static mappedBy = [ext: "kitchenSink"]


    //used for event testing
    String event
    String beforeValidateCheck
    String stampEvent

    //enums
    Kind kind
    SinkStatus status

    // objects
    SimplePogo getSimplePogo(){
        return new SimplePogo(foo: 'fly')
    }

    Map bazMap
    List<String> stringList
    static hasMany = [stringList: String]

    //bug in grailsCompileStatic requires this on internal enums
    //also, internal enums must always come before the static constraints or it doesn't get set
    @CompileDynamic
    enum Kind {CLIENT, VENDOR, PARENT}

    static mapping = {
        //id generator:'assigned'
        ext column: 'extId' //, cascade: 'none'
        thing column: 'thingId'
        status enumType: 'identity'
    }

    static constraintsMap = [
        secret: [ display: false ],
        inactive: [ required: false ],
        kind: [ nullable: false ],
        sinkLink: [ bindable: true ],
        items: [validate: false]
    ]

    List<SinkItem> getItems(){
        SinkItem.listByKitchenSink(this)
    }

    // static KitchenSink build(Long id){
    //     def loc = new Thing(city: "City$id")
    //     loc.id = id
    //     loc.persist()
    //
    //     def ks = new KitchenSink(
    //         id: id,
    //         num: "$id",
    //         name: "Kitchen$id",
    //         name2: (id % 2) ? "OrgName2" + id : null ,
    //         kind: (id % 2) ? KitchenSink.Kind.VENDOR : KitchenSink.Kind.CLIENT ,
    //         status: (id % 2) ? SinkStatus.Inactive : SinkStatus.Active,
    //         inactive: (id % 2 == 0),
    //         amount: (id - 1) * 1.25,
    //         actDate: LocalDateTime.now().plusDays(id).toDate(),
    //         localDate: LocalDate.now().plusDays(id),
    //         localDateTime: LocalDateTime.now().plusDays(id),
    //         thing: loc
    //     ).persist()
    //     ks.ext = new SinkExt(
    //         kitchenSink: ks,
    //         name: "ext$id",
    //         kitchenParent: id % 2 == 0 ? KitchenSink.load(1) : KitchenSink.load(2)
    //     ).persist()
    //     return ks
    // }

    static KitchenSink build(Long id){
        return getRepo().build(id)
    }

    static List<Map> generateDataList(int numRecords, Map extraData = [:]) {
        return getRepo().generateDataList(numRecords, extraData)
    }
}
