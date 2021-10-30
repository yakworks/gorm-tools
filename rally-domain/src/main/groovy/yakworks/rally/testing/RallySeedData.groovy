/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.testing

import java.time.LocalDate

import groovy.transform.CompileStatic

import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.beans.AppCtx
import grails.compiler.GrailsCompileStatic
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.model.OrgTypeSetup
import yakworks.rally.tag.model.Tag

// import grails.buildtestdata.TestData
@SuppressWarnings('BuilderMethodWithSideEffects')
@GrailsCompileStatic
class RallySeedData {

    static JdbcTemplate jdbcTemplate

    static init(){
        jdbcTemplate = AppCtx.get("jdbcTemplate", JdbcTemplate)
    }

    static fullMonty(){
        buildOrgs(100)
        buildTags()
        createIndexes()
    }
    static void createOrgTypeSetups(){
        OrgType.values().each {
            new OrgTypeSetup(id: it.id, name: it.name()).persist(flush:true)
        }
    }

    static void buildOrgs(int count){
        Org.withTransaction {
            //createOrgTypeSetups()
            (1..2).each{
                def company = createOrg(it , OrgType.Company)
                company.location.kind = Location.Kind.remittance
                company.location.persist()
            }
            if(count < 3) return
            (3..count).each { id ->
                def org = createOrg(id, OrgType.Customer)
            }
        }

    }

    static Org createOrg(Long id, OrgType type){
        def con = Contact.create(
            id: id,
            num: "primary$id",
            email    : "jgalt$id@taggart.com",
            firstName: "John$id",
            lastName : "Galt$id"
        )

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
            contact: [
                id: con.id
            ],
            contacts: [[
                num: "secondary$id",
                email    : "secondary$id@taggart.com",
                firstName: "firstName$id",
                lastName : "lastName$id"
            ]]
        ]
        def org = Org.create(data)
        // assert org.flex.date1.toString() == '2021-04-20'
        return org
    }

    static void buildTags(){
        Tag.withTransaction {
            def t1 =new Tag(id: 1, code: "CPG", entityName: 'Customer').persist(flush: true)
            def t2 =new Tag(id: 2, code: "MFG", entityName: 'Customer').persist(flush: true)
            assert Tag.findById(1)
        }
    }

    static void createIndexes(){
        Tag.withTransaction {
            jdbcTemplate.execute("CREATE UNIQUE INDEX ix_OrgSource_unique on OrgSource(sourceType,sourceId,orgTypeId)")
        }
    }

}
