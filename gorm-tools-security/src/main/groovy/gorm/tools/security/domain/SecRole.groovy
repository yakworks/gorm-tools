/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import groovy.transform.EqualsAndHashCode

import gorm.tools.model.NameCodeDescription
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
class SecRole implements NameCodeDescription, RepoEntity<SecRole>, Serializable {

    static final String ADMIN = "ADMIN" //full access, system user

    String name
    Boolean inactive = false

    void beforeValidate() {
        if(!this.name && this.code) this.name = code.replaceAll('-', ' ').replaceAll('_', ' ')
        if(this.name && !this.code) this.code = name.replaceAll(' ', '_')
        if(code.toUpperCase() != code) code = code.toUpperCase()
    }

    static constraintsMap = [
        code:[ d: 'Upper case role key',
               nullable: false, maxSize: 30, matches: "[A-Z0-9-_]+" ],
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
