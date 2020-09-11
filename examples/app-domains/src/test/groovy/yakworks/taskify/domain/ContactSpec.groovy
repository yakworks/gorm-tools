/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.taskify.domain

import java.time.LocalDateTime

import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification

class ContactSpec extends Specification implements DomainRepoTest<Contact> {

    void "CRUD tests"() {
        expect:
        createEntity().id
        persistEntity().id
        updateEntity().version > 0
        removeEntity()
    }

    void "did it get the audit stamp fields"() {
        when:
        def con = build()
        con.validate()

        def conProps = Contact.constrainedProperties
        then:
        // con.editedBy == 0
        // con.editedDate.withNano(0) == LocalDateTime.now().withNano(0)
        //sanity check the main ones
        conProps.firstName.nullable == false
        conProps['inactive'].property.metaConstraints["bindable"] == false
        conProps.inactive.display == false
        conProps['inactive'].property.display == false
        conProps['inactive'].editable == false

        // conProps['editedBy'].nullable == false
        // conProps['editedDate'].nullable == false
        conProps['editedBy'].property.metaConstraints["bindable"] == false
        // conProps['editedBy'].property.metaConstraints == [:]
        // conProps['editedBy'].editable == false
        // conProps['editedBy'].property.editable == false
        // conProps['editedBy'].property.display == false

        ['editedBy','createdBy', 'editedDate','createdDate'].each {
            assert con.hasProperty(it)
            def conProp = conProps[it].property
            conProp.metaConstraints["bindable"] == false
            assert conProp.nullable
            assert !conProp.display
            assert !conProp.editable
            // !conProps[it].editable
            // !conProps[it].bindable
        }

        // Contact.constrainedProperties.collect {
        //     [it.value.getPropertyName(), it.value.property.appliedConstraints]
        // } == []

    }

}
