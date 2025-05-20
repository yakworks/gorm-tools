/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.jobqueue

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils

import gorm.tools.job.SyncJobEntity
import yakworks.gorm.api.bulk.BulkImportService
import yakworks.rally.job.SyncJob
import yakworks.testing.gorm.model.KitchenSink

@Component
@Slf4j
@CompileStatic
class QueuedJobRunner {

    void runJob(SyncJob syncJob){
        log.info("🤡    runJob called: $syncJob")
        //sleep(2000)
        String jobType = syncJob.jobType
        if(jobType == 'bulk.import'){
            runBulkImport(syncJob)
        }
        else if (jobType == 'bulk.export'){
            runBulkExport(syncJob)
        }

    }

    void runBulkImport(SyncJob syncJob){
        String entityClassName = syncJob.params['entityClassName']
        Class<?> entityClass = ClassUtils.resolveClassName(entityClassName, null);
        var bulkImportService = BulkImportService.lookup(entityClass)
        SyncJobEntity jobEnt2 = bulkImportService.startJob(syncJob.id)
        //sleep for a couple seconds to simulate larger data
        sleep(2000)
        KitchenSink.withTransaction {
            log.info("🤡    Job Id Completed: ${jobEnt2}, KitchenSink count: ${KitchenSink.count()}")
        }
    }

    void runBulkExport(SyncJob syncJob){

    }
}
