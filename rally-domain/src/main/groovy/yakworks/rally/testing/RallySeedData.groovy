/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.testing

import java.time.LocalDate

import groovy.transform.CompileStatic

import org.springframework.jdbc.core.JdbcTemplate

import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.TaskType
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.OrgMemberService
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactFlex
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.OrgTypeSetup
import yakworks.rally.orgs.repo.ContactRepo
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRolePermission
import yakworks.security.gorm.model.SecRoleUser
import yakworks.security.gorm.testing.SecuritySeedData
import yakworks.spring.AppCtx

@SuppressWarnings('BuilderMethodWithSideEffects')
@CompileStatic
class RallySeedData {

    static JdbcTemplate jdbcTemplate

    /** the classes to mock for unit tests, NOTE: stackoverflow if this is not specifed with generics */
    static List<Class<?>> getEntityClasses() {
        return [
            AppUser, SecRole, SecRoleUser, SecRolePermission,
            Org, OrgSource, OrgTypeSetup, OrgTag, OrgMember, OrgFlex, OrgCalc, OrgInfo,
            Location, Contact,
            ActivityLink, Activity, TaskType, ActivityNote, ActivityContact,
            Tag, TagLink
        ] as List<Class<?>>
    }

    static springBeans = [
        orgDimensionService: OrgDimensionService,
        orgMemberService: OrgMemberService
    ]

    static init(){
        jdbcTemplate = AppCtx.get("jdbcTemplate", JdbcTemplate)
        //gorm creation scripts dont set constraints to not null so we do it here
        Org.withTransaction {
            jdbcTemplate.execute("ALTER TABLE Contact ALTER orgId SET NOT NULL;")
            jdbcTemplate.execute("ALTER TABLE Location ALTER orgId SET NOT NULL;")
        }
    }

    static fullMonty(int count = 100){
        if(!jdbcTemplate) init()

        buildAppUsers()
        createOrgTypeSetups()
        buildClientOrg()
        buildOrgs(count, true)
        buildTags()
        createIndexes()
    }

    static void createOrgTypeSetups(){
        OrgType.values().each {
            String code = it == OrgType.CustAccount ? 'CustAcct': it.name()
            new OrgTypeSetup(id: it.id, code: code, name: it.name()).persist(flush:true)
        }
    }

    /**
     * build the Organizations up to count.
     * Will create 2 company accounts, 2 brnaches and 2 divisions first.
     * the rest will be customers.
     * @param count the count to make
     * @param createContact if true will generate a default contact for each org as well.
     */
    static void buildOrgs(int count, boolean createContact = false){
        Org.withTransaction {
            //createOrgTypeSetups()
            (2..3).each{
                def company = createOrg(it , OrgType.Company, createContact)
                company.location.kind = Location.Kind.remittance
                company.location.persist()
            }
            (4..5).each{
                def branch = createOrg(it , OrgType.Branch, createContact)
                branch.persist()
            }
            (6..7).each{
                def division = createOrg(it , OrgType.Division, createContact)
                division.persist()
            }
            if(count < 7) return
            (8..count).each { id ->
                def org = createOrg(id, OrgType.Customer, createContact)
            }
        }

    }

    static void buildClientOrg(){
        Org.withTransaction {
            def client = createOrg(1, OrgType.Client, true)

            client.contact.user = AppUser.get(1)
            client.contact.persist(flush: true)

            assert Contact.query(id: 1).get()
        }
    }

    /**
     * create an org.
     *
     * @param id the id to gen
     * @param type the org type
     * @param createContact whether to create the contact
     * @return the created org
     */
    static Org createOrg(Long id, OrgType type, boolean createContact = false){

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
            location: [city: "City$id"],
            contacts: [[
                num: "secondary$id",
                email    : "secondary$id@taggart.com",
                firstName: "firstName$id",
                lastName : "lastName$id"
            ]]
        ]
        def org = Org.create(data, bindId: true)
        org.calc = new OrgCalc(id: org.id, totalDue: id*10.0).persist()

        Org.repo.flush()

        if(createContact){
            Contact contact = makeContact(org, [id: org.id])
            org.contact = contact
            org.persist()
        }

        return org
    }

    static Contact makeContact(Org org, Map odata = [:]){
        ContactRepo contactRepo = Contact.repo

        Long id = odata.id as Long
        //always force an id, so get early if not specified
        if(!id) id = contactRepo.generateId()

        def data = [
            id: id,
            num: "C$id",
            email    : "jgalt$id@taggart.com",
            firstName: "John$id",
            lastName : "Galt$id",
            org: org
        ]
        //override with whatever was passed in
        data.putAll(odata ?: [:])
        Map args = odata.id ? [bindId: true] : [:]

        def contact = Contact.repo.create(data, args)
        return contact
    }

    static void buildOrgMembers() {
        //if its a cust then add members
        Org.list().each { Org org ->
            if(org.type == OrgType.Customer) {
                Long id = org.id
                org.member = new OrgMember(
                    id: id, org: org,
                    branch: (id % 2) ? Org.load(4): Org.load(5),
                    division: (id % 2) ? Org.load(6): Org.load(7)
                ).persist()
            }
        }

    }

    static Activity makeNote(Org org, String note = 'Test note') {
        return Activity.create(org: org, note: [body: note])
    }

    static void buildTags(){
        Tag.withTransaction {
            def t1 = new Tag(id: 1 as Long, code: "CPG", entityName: 'Customer').persist(flush: true)
            def t2 = new Tag(id: 2 as Long, code: "MFG", entityName: 'Customer').persist(flush: true)
        }
    }

    static void createIndexes(){
        Tag.withTransaction {
            jdbcTemplate.execute("CREATE UNIQUE INDEX ix_OrgSource_unique on OrgSource(sourceType,sourceId,orgTypeId)")
        }
    }

    static void buildAppUsers(){
        AppUser.withTransaction {
            SecuritySeedData.fullMonty()
        }
    }

}
