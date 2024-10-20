/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.json

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

import org.grails.datastore.gorm.GormEntity
import org.hibernate.annotations.NaturalId
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef

import grails.compiler.GrailsCompileStatic
import io.hypersistence.utils.hibernate.type.json.JsonType

@Entity(name = "JpaEnt")
@TypeDef(name = "json", typeClass = JsonType.class)
@GrailsCompileStatic
class JpaEnt implements GormEntity<JpaEnt> {
// class JpaEnt implements Persistable<Long>, GormEntity<JpaEnt>, RepoEntity<JpaEnt> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Column(length = 15)
    private String isbn;

    @Type(type = "json")
    // @Column(columnDefinition = "json")
    Map json = [:]

    // static mapping = {
    //     id generator: "uuid2"
    //     json type: JsonType, sqlType: 'json'
    // }

    // static constraints = {
    //     name nullable: false
    // }
}
