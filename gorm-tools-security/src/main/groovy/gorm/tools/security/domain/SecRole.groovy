/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import groovy.transform.EqualsAndHashCode

import gorm.tools.model.NameDescription
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

/**
 * SecRole class for Authority.
 */

@Entity
@EqualsAndHashCode(includes='name', useCanEqual=false)
@GrailsCompileStatic
class SecRole implements NameDescription, RepoEntity<SecRole>, Serializable {

    static final String ADMINISTRATOR = "Administrator" //full access, system user
    static final String ADMIN = "Administrator" //.Alias

    static transients = ['springSecRole']

    String name
    Boolean inactive = false

    static constraintsMap = [
        name: [d: "The name of the role",
            nullable: false, maxSize: 20],
        description: [d: "A longer description",
            nullable: true, maxSize: 255],
        inactive: [d: "Whether role should be active", oapi:'U']
    ]

    static mapping = orm {
        cache "read-write"
    }

    /**
     * Spring security plugin needs all authorities to be prefixed with ROLE_ and hence all roles must
     * be saved in database with name such as ROLE_MANAGER etc. However we use custom user detail service,
     * and call getSpringSecRole when populating a authorities for the UserDetail.
     * it allows us to save role names in db without prefix ROLE_
     */
    String getSpringSecRole() {
        return "ROLE_${name}".toString().toUpperCase()
    }
}
