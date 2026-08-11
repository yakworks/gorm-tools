/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.model

import groovy.transform.ToString
import yakworks.security.auditable.Auditable

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileDynamic

import gorm.tools.model.NameNum
import gorm.tools.repository.model.RepoEntity
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
@ToString(includeNames=true, includes = ["id", "num"])
class KitchenSink implements NameNum, RepoEntity<KitchenSink>, Serializable, Auditable  {

    //<- ext belong to KitchenSink
    // since ext also has an KitchenSink property (kitchenParent) it will confused
    // example of how to explcitly force the "belongsTo"  with the mappedBy
    static mappedBy = [ext: "kitchenSink"]
    static hasMany = [stringList: String]
    static toOneAssociations = [ 'ext' ]

    static Map includes = [
        qSearch: ['id', 'num', 'name'],
        stamp: ['id', 'num', 'name']
    ]

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

    Long createdByJobId

    //bug in grailsCompileStatic requires this on internal enums
    //also, internal enums must always come before the static constraints or it doesn't get set
    @CompileDynamic
    enum Kind {CLIENT, VENDOR, PARENT}

    static mapping = {
        //id generator:'assigned'
        ext column: 'extId', lazy: true
        thing column: 'thingId', lazy: true
        status enumType: 'identity'
        sinkLink column: "sinkLinkId"
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
