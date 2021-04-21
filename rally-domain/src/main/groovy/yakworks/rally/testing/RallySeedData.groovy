/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.testing

import java.time.LocalDate

import groovy.transform.CompileStatic

import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.OrgTypeSetup

// import grails.buildtestdata.TestData

@CompileStatic
class RallySeedData {

    static void createOrgTypeSetups(){
        OrgType.values().each {
            new OrgTypeSetup(id: it.id, name: it.name()).persist(flush:true)
        }
    }

    @SuppressWarnings('BuilderMethodWithSideEffects')
    static void buildOrgs(int count){
        createOrgTypeSetups()
        (1..2).each{
            def company = createOrg(it , OrgType.Company)
            company.location.kind = Location.Kind.remittance
            company.location.persist()
        }
        (3..count).each { id ->
            def org = createOrg(id, OrgType.Customer)
        }
    }

    static Org createOrg(Long id, OrgType type){

        String value = "Org" + id
        def data = [
            id: id,
            num: "$id",
            name: value,
            type: type,
            comments: (id % 2) ? "Lorem ipsum dolor sit amet $id" : null ,
            inactive: (id % 2 == 0),
            info: [phone: "1-800-$id"],
            flex: [
                num1: (id - 1) * 1.25,
                num2: (id - 1) * 1.5,
                date1: LocalDate.now().plusDays(id).toString()
            ],
            location: [city: "City$id"]
        ]
        def org = Org.create(data)
        // assert org.flex.date1.toString() == '2021-04-20'
        return org
    }

}
