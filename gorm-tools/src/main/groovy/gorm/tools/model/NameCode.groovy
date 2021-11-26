/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import javax.persistence.Transient

import groovy.transform.CompileStatic

import gorm.tools.repository.RepoLookup

@SuppressWarnings(['MethodName'])
@CompileStatic
trait NameCode<D> extends NamedEntity implements Lookupable<D> {

    String code

    // like toString but as a field and for sending across the wire
    @Transient
    String getStamp(){ "${getCode()} : ${getName()}"}

    static Map includes = [
        qSearch: ['name', 'code'],
        stamp: ['id', 'code', 'name']  //picklist or minimal for joins
    ]

    static constraintsMap = [
        code:[ d: 'Short code, alphanumeric with no special characters except dash (for space) and underscore',
               nullable: false, maxSize: 10, matches: "[a-zA-Z0-9-_]+" ]
    ]

    void beforeValidate() {
        if(!this.name && this.code) this.name = code.replaceAll('-', ' ')
    }

    //FIXME #339 framed out example
    static D lookup(Map data){
        if(data['code']) {
            (D) RepoLookup.findRepo(this).query(code: data['code']).get()
        }
    }


}
