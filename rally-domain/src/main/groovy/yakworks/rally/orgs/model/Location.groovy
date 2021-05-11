/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import groovy.transform.CompileDynamic

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.orgs.repo.LocationRepo

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class Location implements GormRepoEntity<Location, LocationRepo>, Serializable {
    static transients = ['addressHtml']
    //static belongsTo = [org: Org]
    Kind kind = Kind.work
    String name //description name

    // address fields
    String street1
    String city
    String state //provence
    String zipCode //postalCode
    String country = "US"
    String county

    // org is required and when for contact this will just be contact's org
    //belongsTo org but since it is both a 1toMany and and association on the org we dont use the belongsTo
    Org org
    Contact contact

    //additional for ERP interfaces if needed.
    String street2
    String address3
    String address4

    @CompileDynamic //angry monkey, GrailsCompileStatic bug needs this.
    static enum Kind {
        work, home, other, mailing, remittance, physical

        static List<String> stringValues() {
            return values().toList()*.name() as List<String>
        }
    }

    static constraintsMap = [
        org:[ description: 'The organization this belongs to', nullable: false],
        kind:[ description: 'The address type', nullable: true],

        contact:[ description: 'The contact this belongs to', nullable: true],
        name:[ description: 'A descriptive name, can be used for reports an letters', nullable: true],

        // address fields
        street1:[maxSize: 100],
        street2:[maxSize: 100],
        city:[maxSize: 100],
        state:[maxSize: 25],
        zipCode:[maxSize: 50],
        country:[maxSize: 3],
        county:[maxSize: 50],
        address3:[maxSize: 100],
        address4:[maxSize: 100],
    ]

    static mapping = {
        //columns
        id generator: 'assigned'
        org column: 'orgId'
        contact column: 'contactId'
    }

    String getAddressHtml() {
        String markup = ""
        if (street1)
            markup = "${street1.trim()} <br/>"

        if (street2?.trim())
            markup = "$markup ${street2.trim()} <br/>"

        if (city?.trim())
            markup = "$markup ${city.trim()},"

        if (state?.trim())
            markup = "$markup ${state.trim()}"

        if (zipCode?.trim())
            markup = "$markup ${zipCode.trim()}"

        return markup.trim()
    }

    static List<Location> listByContact(Contact con){
        Location.where { contact == con }.list()
    }

    static List<Location> listByOrgOnly(Org o){
        Location.where { org == o && contact == null}.list()
    }

}
