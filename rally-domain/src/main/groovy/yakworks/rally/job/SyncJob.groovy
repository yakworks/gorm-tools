/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import gorm.tools.audit.AuditStamp
import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.gorm.annotation.Entity

/**
 * An instance created right away when "any job" in 9ci is called.
 * Either called through restApi from outside, scheduled job in quartz or
 * manually started job (Look at SourceTrait)
 * Job may no longer exist to query. 9ci only logs the last 100 jobs. Jobs also expire within an hour.
 */
@Entity
@AuditStamp
@GrailsCompileStatic
class SyncJob implements SyncJobEntity<SyncJob>, Serializable {

    /**
     * gets the payloadData as byte array, either from attachment file or payload byte array
     * @return
     */
    byte[] getPayloadData(){
        return getRepo().getPayloadData(this)
    }

    /**
     * The data is a response of resources that were successfully and unsuccessfully updated or created after processing.
     * gets the data as byte array, either from attachment file or resultData byte array
     */
    byte[] getData(){
        return getRepo().getData(this)
    }

    static SyncJobRepo getRepo() { RepoLookup.findRepo(this) as SyncJobRepo }

    static mapping = {
        state column: 'state', enumType: 'identity'
    }

}
