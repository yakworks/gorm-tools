/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans.domain

import groovy.transform.CompileStatic

import yakworks.commons.model.IdEnum
import gorm.tools.repository.model.RepoEntity
import grails.persistence.Entity

@Entity
class Bookz implements RepoEntity<Bookz> {
    String name
    BigDecimal cost
    String hiddenProp

    Bookz(){
        id = 1
    }

    static transients = ['company']

    String getCompany() {
        'Tesla'
    }

    static hasMany = [stringList: String]

    //See https://sysgears.com/articles/advanced-gorm-features-inheritance-embedded-data-maps-and-lists-storing/
    List<String> stringList

    Map bazMap

    SimplePogo getSimplePogo(){
        return new SimplePogo(name: 'fly')
    }

    List<BookTag> getBookTags(){
        return [new BookTag(name: 'red'), new BookTag(name: 'green')] as List<BookTag>
    }

    static mapping = {
        id generator:'assigned'
    }
    static constraints = {
        hiddenProp display: false
    }

}

@Entity
class BookAuthor {
    BookAuthor(){
        id = 2
    }
    Bookz book
    BookAuthor bookAuthor
    Integer age
    String name

    static mapping = {
        id generator:'assigned'
    }
}

class SimplePogo {
    String name
}


@Entity
class BookTag {
    String name
}

class PropsToMapTest {
    String field
    long field2
    Long field3
    Boolean field4
    boolean field5
    Map field6
    List field7
    BigDecimal field8
    Double field9
    PropsToMapTest nested
}

class PogoTest {
    String fString
    long flong
    Long fLong
    Boolean fBoolean
    boolean fboolean
    Map fMap
    List fList
    BigDecimal fBigDecimal
    Double fDouble
    PropsToMapTest nested
}
