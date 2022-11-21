/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

import static yakworks.json.groovy.JsonEngine.parseJson

@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class TestSyncJob implements RepoEntity<TestSyncJob>, SyncJobEntity<TestSyncJob> {

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
    }
}
