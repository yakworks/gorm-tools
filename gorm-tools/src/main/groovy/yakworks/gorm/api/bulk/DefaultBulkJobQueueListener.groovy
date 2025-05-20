/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.EventListener

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobQueueEvent
import gorm.tools.utils.ServiceLookup
import yakworks.gorm.config.GormConfig

/**
 * Default used for firing bulk jobs
 */
@CompileStatic
class DefaultBulkJobQueueListener {

    @Autowired GormConfig gormConfig

    @EventListener
    void syncJobQueueEvent(SyncJobQueueEvent qe) {
        SyncJobArgs args = qe.syncJobArgs

        if(qe.jobType == 'bulk.import' && !gormConfig.legacyBulk){
            assert args.entityClass
            var bulkImportService = getBulkImportService(args.entityClass)
            bulkImportService.startJob(qe.syncJob.id)
        }
    }

    public <D> BulkImportService<D> getBulkImportService(Class<D> entityClass){
        return BulkImportService.lookup(entityClass)
    }

}
