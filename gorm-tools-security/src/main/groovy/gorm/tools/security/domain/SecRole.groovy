/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import groovy.transform.EqualsAndHashCode

import gorm.tools.model.NameCodeDescription
import gorm.tools.model.NameDescription
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

/**
 * SecRole class for Authority.
 * Spring security plugin needs all authorities to be prefixed with ROLE_ and hence all roles must
 * be saved in database with code such as ROLE_MANAGER etc.
 */
@Entity
@EqualsAndHashCode(includes='name', useCanEqual=false)
@GrailsCompileStatic
class SecRole implements NameCodeDescription, RepoEntity<SecRole>, Serializable {

    static final String ADMINISTRATOR = "ROLE_ADMIN" //full access, system user
    static final String ADMIN = "ROLE_ADMIN" //.Alias

    // static transients = ['springSecRole']

    String name
    Boolean inactive = false

    void beforeValidate() {
        if(!this.name && this.code) this.name = code.replaceAll('-', ' ')
        if(!code.startsWith('ROLE_')) code =  "ROLE_${code}".toString().toUpperCase()
        if(code.toUpperCase() != code) code = code.toUpperCase()
    }

    static constraintsMap = [
        code:[ d: 'Upper case role key, starts with ROLE_ always',
               nullable: false, maxSize: 10, matches: "[A-Z0-9-_]+" ],
        name: [d: "The name of the role",
            nullable: false, maxSize: 20],
        description: [d: "A longer description",
            nullable: true, maxSize: 255],
        inactive: [d: "Whether role should be active", oapi:'U']
    ]

    static mapping = orm {
        cache "read-write"
    }

}
