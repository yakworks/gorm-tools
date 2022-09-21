/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.model

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileDynamic

import gorm.tools.hibernate.criteria.CreateCriteriaSupport
import gorm.tools.model.NameNum
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.security.audit.AuditStamp

/**
 * A sample domain model for ktichen sink testing
 */
@AuditStamp
@IdEqualsHashCode
@Entity
// @ManagedEntity //see ManagedEntitySinkSpec
@GrailsCompileStatic
class KitchenSink implements NameNum, GormRepoEntity<KitchenSink, KitchenSinkRepo>, CreateCriteriaSupport {
    //<- ext belong to KitchenSink
    // since ext also has an KitchenSink property (kitchenParent) it will confused
    // example of how to explcitly force the "belongsTo"  with the mappedBy
    static mappedBy = [ext: "kitchenSink"]
    static hasMany = [stringList: String]
    static toOneAssociations = [ 'ext' ]

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


    //bug in grailsCompileStatic requires this on internal enums
    //also, internal enums must always come before the static constraints or it doesn't get set
    @CompileDynamic
    enum Kind {CLIENT, VENDOR, PARENT}

    static mapping = {
        //id generator:'assigned'
        ext column: 'extId', lazy: true
        thing column: 'thingId', lazy: true
        status enumType: 'identity'
    }

    static constraintsMap = [
        secret: [ display: false ],
        inactive: [ required: false ],
        kind: [ nullable: false ],
        sinkLink: [ bindable: true ],
        items: [validate: false]
    ]

    static KitchenSinkRepo getRepo() {
        KitchenSinkRepo.INSTANCE
    }

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

    static KitchenSink build(Long id, boolean flushIt = true){
        return getRepo().build(id, flushIt)
    }

    static List<Map> generateDataList(int numRecords, Map extraData = [:]) {
        return getRepo().generateDataList(numRecords, extraData)
    }

    static void truncate() {
        getRepo().deleteAll()
    }

    static void cleanup() {
        KitchenSinkRepo.cleanup()
    }

    static void createKitchenSinks(int i) {
        getRepo().createKitchenSinks(i)
    }
}
