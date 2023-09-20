/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.config.MappingDefinition

import gorm.tools.hibernate.type.JsonType
import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

import static grails.gorm.hibernate.mapping.MappingBuilder.orm
import static yakworks.json.groovy.JsonEngine.parseJson

@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class TestSyncJob implements RepoEntity<TestSyncJob>, SyncJobEntity {

    //parseJson
    <T> T parseData(Class<T> clazz = List){
        parseJson(dataToString(), clazz)
    }

    @Override
    String dataToString(){
        def dta = dataBytes
        return dta ? new String(dta, "UTF-8") : '[]'
    }

    @Override
    String payloadToString(){
        def dta = payloadBytes
        return dta ? new String(dta, "UTF-8") : '[]'
    }


    static TestSyncJobRepo getRepo() { RepoLookup.findRepo(this) as TestSyncJobRepo }

    static mapping = {
        state column: 'state', enumType: 'identity'
        payloadBytes sqlType: 'BLOB'
        dataBytes sqlType: 'BLOB'
        //intelij show this as an error but it works fine
        problems type: JsonType, params: [type: ArrayList]
    }
}
