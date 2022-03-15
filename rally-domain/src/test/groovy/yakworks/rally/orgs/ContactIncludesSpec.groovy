/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs

import gorm.tools.beans.map.MetaMapIncludesBuilder
import gorm.tools.beans.map.MetaMapEntityService
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Specification
import yakworks.gorm.testing.SecurityTest
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactEmail
import yakworks.rally.orgs.model.ContactPhone
import yakworks.rally.orgs.model.Org
import yakworks.rally.testing.MockData

class ContactIncludesSpec extends Specification implements DataRepoTest, SecurityTest {

    MetaMapEntityService metaMapEntityService

    void setupSpec() {
        mockDomains Org, Contact, ContactPhone, ContactEmail
    }

    void "EntityIncludesBuilder.build"(){
        when:
        def res = MetaMapIncludesBuilder.build(Contact, ['*'])

        then:
        res.className == 'yakworks.rally.orgs.model.Contact'
        // res.fields == ['name'] as Set
    }

    void "createMetaMap"() {
        when:
        List<Map> emails = [[kind: "work", address: "test@9ci.com"]]
        Map dta = [firstName: "bill", emails:emails]
        Contact contact = MockData.contact(dta)

        def result = metaMapEntityService.createMetaMap(contact, ['id', 'emails.$*'])

        then:
        result == [id:1, emails:[[id:1, address:'test@9ci.com', kind:'work']]]
    }
}
