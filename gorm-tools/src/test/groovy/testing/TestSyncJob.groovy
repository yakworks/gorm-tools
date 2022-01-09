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

@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class TestSyncJob implements SyncJobEntity<TestSyncJob> {

    String message  // not sure if needed

    /**
     * gets the payloadData as byte array, either from attachment file or payload byte array
     * @return
     */
    byte[] getPayloadData(){
        return payloadBytes
    }

    /**
     * The data is a response of resources that were successfully and unsuccessfully updated or created after processing.
     * gets the data as byte array, either from attachment file or resultData byte array
     */
    byte[] getData(){
        return dataBytes
    }

    static TestSyncJobRepo getRepo() { RepoLookup.findRepo(this) as TestSyncJobRepo }

    static mapping = {
        state column: 'state', enumType: 'identity'
    }
}
