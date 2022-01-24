/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.testing

import java.time.LocalDate

import groovy.transform.CompileStatic

import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.beans.AppCtx
import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import yakworks.rally.activity.model.Activity
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactFlex
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.OrgTypeSetup
import yakworks.rally.tag.model.Tag

@SuppressWarnings('BuilderMethodWithSideEffects')
@CompileStatic
class RallySeedData {

    static JdbcTemplate jdbcTemplate

    static init(){
        jdbcTemplate = AppCtx.get("jdbcTemplate", JdbcTemplate)
        //gorm creation scripts dont set constraints to not null so we do it here
        Org.withTransaction {
            jdbcTemplate.execute("ALTER TABLE Contact ALTER orgId SET NOT NULL;")
            jdbcTemplate.execute("ALTER TABLE Location ALTER orgId SET NOT NULL;")
        }
    }

    static fullMonty(){
        buildAppUser()
        createOrgTypeSetups()
        buildClientOrg()
        buildOrgs(100)
        buildTags()
        createIndexes()
    }

    static void createOrgTypeSetups(){
        OrgType.values().each {
            String code = it == OrgType.CustAccount ? 'CustAcct': it.name()
            new OrgTypeSetup(id: it.id, code: code, name: it.name()).persist(flush:true)
        }
    }

    static void buildOrgs(int count){
        Org.withTransaction {
            //createOrgTypeSetups()
            (2..3).each{
                def company = createOrg(it , OrgType.Company)
                company.location.kind = Location.Kind.remittance
                company.location.persist()
            }
            (4..5).each{
                def branch = createOrg(it , OrgType.Branch)
                branch.persist()
            }
            (6..7).each{
                def division = createOrg(it , OrgType.Division)
                division.persist()
            }
            if(count < 7) return
            (8..count).each { id ->
                def org = createOrg(id, OrgType.Customer)
            }
        }

    }

    static void buildClientOrg(){
        Org.withTransaction {
            def client = createOrg(1, OrgType.Client)

            client.contact.user = AppUser.get(1)
            client.contact.persist(flush: true)

            assert Contact.query(id: 1).get()
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
            location: [city: "City$id"],
            contacts: [[
                num: "secondary$id",
                email    : "secondary$id@taggart.com",
                firstName: "firstName$id",
                lastName : "lastName$id"
            ]]
        ]
        def org = Org.create(data, bindId: true)
        Org.repo.flush()

        def contact = new Contact(
            id: id,
            num: "primary$id",
            email    : "jgalt$id@taggart.com",
            firstName: "John$id",
            lastName : "Galt$id",
            org: org,
            flex: new ContactFlex(
                num1: id * 1.01
            )
        )
        contact.user = AppUser.get(1)
        contact.persist()
        Org.repo.flush()

        org.contact = contact
        org.persist()

        // add note
        def act = Activity.create([id:id, org: org, note: [body: 'Test note']], bindId: true)
        // assert org.flex.date1.toString() == '2021-04-20'
        return org
    }

    static void buildTags(){
        Tag.withTransaction {
            def t1 = new Tag(id: 1, code: "CPG", entityName: 'Customer').persist(flush: true)
            def t2 = new Tag(id: 2, code: "MFG", entityName: 'Customer').persist(flush: true)
        }
    }

    static void createIndexes(){
        Tag.withTransaction {
            jdbcTemplate.execute("CREATE UNIQUE INDEX ix_OrgSource_unique on OrgSource(sourceType,sourceId,orgTypeId)")
        }
    }

    static void buildAppUser(){
        AppUser.withTransaction {
            AppUser user = new AppUser(id: 1, username: "admin", email: "admin@9ci.com", password:"123Foo")
            user.persist()
            assert user.id == 1

            SecRole admin = new SecRole(id:1, name: SecRole.ADMINISTRATOR).persist()
            SecRole power = new SecRole(id:2, name: "Power User").persist()
            SecRole guest = new SecRole(id:3, name: "Guest").persist()

            SecRoleUser.create(user, admin, true)
            SecRoleUser.create(user, power, true)

            AppUser noRoleUser = AppUser.create([id: 2, username: "noroles", email: "noroles@9ci.com", password:"123Foo"], bindId: true)
            assert noRoleUser.id == 2
            return
        }
    }
}
