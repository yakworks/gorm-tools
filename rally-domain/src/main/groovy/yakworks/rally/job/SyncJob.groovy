/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import groovy.transform.CompileDynamic

import org.grails.datastore.mapping.config.MappingDefinition

import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.gorm.annotation.Entity
import yakworks.gorm.hibernate.type.JsonType
import yakworks.security.audit.AuditStampTrait

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

/**
 * An instance created right away when "any job" in 9ci is called.
 * Either called through restApi from outside, scheduled job in quartz or
 * manually started job (Look at SourceTrait)
 * Job may no longer exist to query. 9ci only logs the last 100 jobs. Jobs also expire within an hour.
 */
@Entity
@GrailsCompileStatic
class SyncJob implements RepoEntity<SyncJob>, SyncJobEntity,  AuditStampTrait, Serializable {

    // List<Map> problems

    byte[] getData(){
        getRepo().getData(this)
    }

    byte[] getPayload(){
        getRepo().getPayload(this)
    }

    @Override
    String dataToString(){
        getRepo().dataToString(this)
    }

     // @Override
     // String problemsToString(){
     //     getRepo().errorToString(this)
     // }

    @Override
    String payloadToString(){
        getRepo().payloadToString(this)
    }

    static SyncJobRepo getRepo() { RepoLookup.findRepo(this) as SyncJobRepo }

    @CompileDynamic
    static MappingDefinition getMapping() {
        orm {
            columns(
                problems: property(type: JsonType, typeParams: [type: ArrayList])
            )
        }
    }

    static constraintsMap = [
        payload:[ d: 'The payload this job was sent to process', oapi: [type: 'object']],
        data: [d: 'The result data json, will normally be an array with items for errors.', oapi: [type: 'object']]
    ]

}
