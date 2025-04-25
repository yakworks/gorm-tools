/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.metamap.MetaGormEntityBuilder
import gorm.tools.metamap.services.MetaMapService
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactEmail
import yakworks.rally.orgs.model.ContactPhone
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Org
import yakworks.rally.testing.MockData
import yakworks.security.gorm.model.SecRoleUser
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class MetaMapIncludesSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [Org, Contact, ContactSource, ContactPhone, ContactEmail, SecRoleUser]

    @Autowired MetaMapService metaMapService

    void "EntityIncludesBuilder.build"(){
        when:
        def res = MetaGormEntityBuilder.build(Contact, ['*'])

        then:
        res.className == 'yakworks.rally.orgs.model.Contact'
        // res.fields == ['name'] as Set
    }

    //XXX Fails after moving this to a GormHibernateTest
    @Ignore
    void "createMetaMap"() {
        when:
        List<Map> emails = [[kind: "work", address: "test@9ci.com"]]
        Map dta = [firstName: "bill", emails:emails]
        Contact contact = MockData.contact(dta)

        def result = metaMapService.createMetaMap(contact, ['id', 'emails.$*'])

        then:
        result == [id:1, emails:[[id:1, address:'test@9ci.com', kind:'work']]]
    }

    void "build for roleUser"() {
        when:
        def incs = MetaGormEntityBuilder.build(SecRoleUser, ["user.id", "user.username", "role.id", "role.name"])

        then:
        noExceptionThrown()
        incs
        incs.metaProps.size() == 2
        incs.metaProps['user']
        incs.metaProps['role']
    }
}
