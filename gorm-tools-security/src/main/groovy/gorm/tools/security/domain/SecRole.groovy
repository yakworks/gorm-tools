/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import groovy.transform.EqualsAndHashCode

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
class SecRole implements RepoEntity<SecRole>, Serializable {

    static final String ADMINISTRATOR = "Administrator" //full access, system user
    static transients = ['springSecRole']

    Boolean inactive = false

    String description
    String name

    static constraints = {
        name description: "The name of the role",
            blank: false, nullable: false, maxSize: 20
        description description: "A longer description",
            nullable: true, maxSize: 255
    }

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
        return "ROLE_${name}".toString()
    }
}
