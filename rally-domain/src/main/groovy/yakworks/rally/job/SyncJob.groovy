/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.RepoLookup
import grails.compiler.GrailsCompileStatic
import grails.gorm.annotation.Entity
import yakworks.security.audit.AuditStamp

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

    byte[] getData(){
        getRepo().getData(this)
    }

    @Override
    String dataToString(){
        getRepo().dataToString(this)
    }
    @Override
    String errorToString(){
        getRepo().errorToString(this)
    }

    @Override
    String payloadToString(){
        getRepo().payloadToString(this)
    }

    static SyncJobRepo getRepo() { RepoLookup.findRepo(this) as SyncJobRepo }

    static mapping = {
        state column: 'state', enumType: 'identity'
    }

}
