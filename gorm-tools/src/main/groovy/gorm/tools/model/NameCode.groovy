/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import groovy.transform.CompileStatic

import gorm.tools.repository.RepoUtil
import yakworks.commons.model.Named

@SuppressWarnings(['MethodName'])
@CompileStatic
trait NameCode<D> extends Named implements Lookupable<D> {

    String code

    static Map includes = [
        qSearch: ['name', 'code'],
        picklist: ['id', 'code', 'name']
    ]

    static constraintsMap = [
        name:[ description: 'The name for this entity', nullable: false, blank: false, maxSize: 50],
        code:[ description: 'Short code, alphanumeric with no special characters except dash (for space) and underscore', nullable: false,
               blank: false, maxSize: 10, matches: "[a-zA-Z0-9-_]+"]
    ]

    void beforeValidate() {
        if(!this.name && this.code) this.name = code.replaceAll('-', ' ')
    }

    //FIXME #339 framed out example
    static D lookup(Map data){
        if(data['code']) {
            (D) RepoUtil.findRepo(this).query(code: data['code']).get()
        }
    }


}
