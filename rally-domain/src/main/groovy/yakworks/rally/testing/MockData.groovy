/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.testing

import java.time.LocalDateTime

import groovy.transform.CompileDynamic

import gorm.tools.security.domain.AppUser
import yakworks.rally.activity.model.Activity
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.OrgTypeSetup

@CompileDynamic //ok for testing
class MockData {

    static Org org(Map dta = [:]) {
        Map vals = [num: 'tsla', name: 'Tesla', type: OrgType.Customer]
        vals.putAll(dta?:[:])
        def o = new Org(vals).persist()
        return o
    }

    static OrgType orgType(OrgType type = OrgType.Customer) {
        OrgTypeSetup ots = new OrgTypeSetup(id: type, name: type.name()).persist()
        assert type.typeSetup
        return type
    }

    static List createSomeContacts(){
        Contact contact1 = contact([firstName: "bill"])
        Contact contact2 = contact([firstName: "bob"])
        [contact1, contact2]
    }

    static Contact contact(Map dta = [:]) {
        def orgDta = dta?.remove('org') ?: [:]
        dta.org = org(orgDta)
        Map vals = [firstName: "Ayne"]
        vals.putAll(dta)

        def c = Contact.create(vals)
        return c
    }

    static AppUser user(Map args = [:]) {
        Map contactArgs = args?.remove("contact") ?: [:]
        if(!contactArgs.name) contactArgs.name = args.username
        args.contact = contact(contactArgs)
        AppUser user = AppUser.create(username:"karen", password:"karen", repassword:"karen", email:"karen@9ci.com")
        user.password = "test"
        user.persist()
        return user
    }

    static Contact createContactWithUser(){
        Contact c = contact([firstName: "John", lastName: 'Galt',  email: "galt@9ci.io"])
        // AppUser user = TestData.build(AppUser, [password:"test"])
        AppUser user = new AppUser(username: c.email, email: c.email, password: 'FooBar!')
        stamp(user)
        user.id = c.id
        user.persist()
        c.user = user
        return c
    }

    //build orgType for unit tests
    static OrgTypeSetup buildOrgType(OrgType orgType){
        def ots = new OrgTypeSetup(name: orgType.name(), description: orgType.name())
        ots.id = orgType.id
        return ots.persist()
    }

    static stamp(Object ent){
        ent['createdBy'] = 1
        ent['createdDate'] = LocalDateTime.now()
        ent['editedBy'] = 1
        ent['editedDate'] = LocalDateTime.now()
    }

    static Map baseOrgParams = [
        num: '0011',
        name: 'testComp',
        companyId: 2L,
        type: 'Customer',
        location: [
            zipCode: '60622',
            street1: '123 Main St. ',
            street2: 'Suite 200',
            city   : 'Chicago',
            state  : 'IL',
            country: 'US'
        ],
        contact: [
            email    : 'jgalt@taggart.com',
            firstName: 'John',
            lastName : 'Galt'
        ],
        flex: [
            text1: 'midas',
            num1 : 99.99,
            date1 : '2020-12-01'
        ],
        info: [
            phone  : '(999)991-2121',
            fax    : '(773)555-1212',
            website: 'google.com'
        ],
        locations: [[state  : 'CA'], [state  : 'CO']]
    ]

    static Map getCreateOrg() {
        return baseOrgParams.clone()
    }

    static Map getUpdateOrg(){
        return [
            num: '1234',
            name: 'foo',
            location: [
                zipCode: '60622',
            ],
            contact: [
                firstName: 'Wyatt',
                lastName : 'Oil'
            ],
            flex: [
                text1: 'bar'
            ],
            info: [
                phone  : '555-1212',
            ]
        ]
    }

    Activity createActNote(Long orgId){
        Map params = [
            org:[id: orgId], //org id does not exist
            note:[body: 'Todays test note']
        ]
        Activity act = Activity.create(params)
        flush()
        return act
    }

    Map getActTaskData(Long orgId){
        return [
            org:[id: orgId], //org id does not exist
            task   : [
                dueDate : "2017-04-28",
                priority: 10,
                state   : 1,
                taskType: [id: 1]
            ]
        ]
    }
}
