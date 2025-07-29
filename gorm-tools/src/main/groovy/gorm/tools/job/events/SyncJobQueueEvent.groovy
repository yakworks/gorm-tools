/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job.events

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationEvent

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity

/**
 * Fired when a SyncJob is queued
 */
@CompileStatic
class SyncJobQueueEvent extends ApplicationEvent { //implements ResolvableTypeProvider {

    String jobType
    SyncJobEntity syncJob
    SyncJobArgs syncJobArgs
    //Class entityClass //domain class for which this sync job is

    SyncJobQueueEvent(SyncJobEntity syncJob) {
        super(syncJob)
        this.jobType = syncJob.jobType
        this.syncJob = syncJob
        //this.entityClass = entityClass ?: Object //if no entityClass - eg for exportSync
    }

    // @Override
    // ResolvableType getResolvableType() {
    //     return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(entityClass))
    // }
}
