/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.seed

import java.time.LocalDate

import groovy.transform.CompileStatic

import org.springframework.jdbc.core.JdbcTemplate

import grails.gorm.transactions.Transactional
import yakworks.rally.activity.ActivityUtil
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.TaskType
import yakworks.rally.orgs.OrgDimensionService
import yakworks.rally.orgs.OrgMemberService
import yakworks.rally.orgs.model.Contact
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
import yakworks.rally.orgs.repo.OrgRepo
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
class RallySeed {

    JdbcTemplate jdbcTemplate

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

    //extra spring beans when orgMember and orgDimensionService is being used.
    static springBeans = [
        orgDimensionService: OrgDimensionService,
        orgMemberService: OrgMemberService
    ]

    // see good explanation of thread safe static instance stratgey https://stackoverflow.com/a/16106598/6500859
    @SuppressWarnings('UnusedPrivateField')
    private static class Holder { private static final RallySeed INSTANCE = new RallySeed().build(); }

    static RallySeed getInstance() {  return Holder.INSTANCE  }

    RallySeed build() {
        jdbcTemplate = AppCtx.get("jdbcTemplate", JdbcTemplate)
        Org.withTransaction {
            jdbcTemplate.execute("ALTER TABLE Contact ALTER orgId SET NOT NULL;")
            jdbcTemplate.execute("ALTER TABLE Location ALTER orgId SET NOT NULL;")
        }
        return this
    }

    static fullMonty(int count = 100){
        RallySeed rallySeed = getInstance()
        rallySeed.buildAppUsers()
        rallySeed.createOrgTypeSetups()
        rallySeed.buildClientOrg()
        rallySeed.buildOrgs(count, true)
        rallySeed.buildTags()
        rallySeed.createIndexes()
    }

    @Transactional
    void createOrgTypeSetups(){
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
    @Transactional
    void buildOrgs(int count, boolean createContact = false){
        Org.withTransaction {
            //createOrgTypeSetups()
            (2..3).each{ id ->
                def company = createOrg([id: id , type: OrgType.Company], createContact)
                company.location.kind = Location.Kind.remittance
                company.location.persist()
            }
            (4..5).each{ id ->
                def branch = createOrg([id: id , type: OrgType.Branch] , createContact)
                branch.persist()
            }
            (6..7).each{ id ->
                def division = createOrg([id: id , type: OrgType.Division] , createContact)
                division.persist()
            }
            if(count < 7) return
            (8..count).each { id ->
                def org = createOrg([id: id , type: OrgType.Customer], createContact)
            }
        }

    }

    @Transactional
    void buildClientOrg(){
        Org.withTransaction {
            def client = createOrg([id: 1 , type: OrgType.Client], true)

            client.contact.user = AppUser.get(1)
            client.contact.persist(flush: true)

            assert Contact.query(id: 1).get()
        }
    }

    /**
     * create an org.
     *
     * @param odata the data for org, requires a minimum of type: OrgType...
     * @param createContact whether to create the contact
     * @return the created org
     */
    @Transactional
    Org createOrg(Map odata, boolean createContact = false){
        OrgRepo orgRepo = Org.repo
        Long id = odata['id'] as Long
        //always force an id, so get early if not specified
        if(!id) id = orgRepo.generateId()

        String value = "Org" + id
        def data = [
            id: id,
            num: "$id",
            name: value,
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
                lastName : "lastName$id",
                location: [city: "second City$id"]
            ]]
        ]
        //override with whatever was passed in
        data.putAll(odata ?: [:])
        Map args = data.id ? [bindId: true] : [:]

        def org = orgRepo.create(data, args)
        org.calc = new OrgCalc(id: org.id, totalDue: id*10.0).persist()

        Org.repo.flush()

        if(createContact){
            Contact contact = makeContact(org, [id: org.id])
            org.contact = contact
            org.persist()
        }
        Activity act = Activity.create([kind: Activity.Kind.Log, orgId: id, name: "created Org ${id}"])
        //add link to test
        act.link(org)
        return org
    }

    @Transactional
    Contact makeContact(Org org, Map odata = [:]){
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
            location: [city: "City$id"],
            org: org
        ]
        //override with whatever was passed in
        data.putAll(odata ?: [:])
        Map args = odata.id ? [bindId: true] : [:]

        def contact = Contact.repo.create(data, args)
        // def loc = new Location(contact: contact, city: "City$id").persist()
        // contact.location = loc
        // contact.persist()
        return contact
    }

    @Transactional
    void buildOrgMembers() {
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

    @Transactional
    Activity makeNote(Org org, String note = 'Test note') {
        return Activity.create(org: org, note: [body: note])
    }

    @Transactional
    void buildTags(){
        def t1 = new Tag(id: 1 as Long, code: "CPG", entityName: 'Customer').persist(flush: true)
        def t2 = new Tag(id: 2 as Long, code: "MFG", entityName: 'Customer').persist(flush: true)
    }

    @Transactional
    void createIndexes(){
        Tag.withTransaction {
            jdbcTemplate.execute("CREATE UNIQUE INDEX ix_OrgSource_unique on OrgSource(sourceType,sourceId,orgTypeId)")
        }
    }

    @Transactional
    void buildAppUsers(){
        SecuritySeedData.fullMonty()
    }

}
