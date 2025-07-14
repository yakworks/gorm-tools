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
import yakworks.gorm.api.bulk.BulkExportJobArgs
import yakworks.gorm.api.bulk.BulkExportService
import yakworks.gorm.api.bulk.BulkImportJobArgs
import yakworks.gorm.api.bulk.BulkImportService
import yakworks.rally.job.SyncJob
import yakworks.testing.gorm.model.KitchenSink

/**
 * POC Bean that runs the jobs.
 * will be called from however we poll the queue.
 * Currently this POC has job that fires every couple seconds that poll the queue
 */
@Component
@Slf4j
@CompileStatic
class QueuedJobRunner {

    void runJob(SyncJob syncJob){
        //sleep(2000)
        String jobType = syncJob.jobType
        log.info("⚙️📡    runJob called with jobType: $jobType - $syncJob")
        if(jobType == BulkImportJobArgs.JOB_TYPE){
            runBulkImport(syncJob)
        }
        else if (jobType == BulkExportJobArgs.JOB_TYPE){
            runBulkExport(syncJob)
        }
        else {
            log.warn("jobType [$jobType] not inplemented")
        }

    }

    void runBulkImport(SyncJob syncJob){
        String entityClassName = syncJob.params['entityClassName']
        Class<?> entityClass = ClassUtils.resolveClassName(entityClassName, null);
        var bulkImportService = BulkImportService.lookup(entityClass)
        SyncJobEntity jobEnt2 = bulkImportService.runJob(syncJob.id)
        //sleep for a couple seconds to simulate larger data
        //sleep(2000)
        KitchenSink.withTransaction {
            log.info("🟢 🗂️🌶️    BULK IMPORT Completed: ${jobEnt2}, KitchenSink total rows: ${KitchenSink.count()}")
        }
    }

    void runBulkExport(SyncJob syncJob){
        String entityClassName = syncJob.params['entityClassName']
        Class<?> entityClass = ClassUtils.resolveClassName(entityClassName, null);
        var bulkExportService = BulkExportService.lookup(entityClass)
        syncJob = (SyncJob) bulkExportService.runJob(syncJob.id)
        //debug some info
        //List dataList = syncJob.parseData()
        log.info("🟢 📤🌶    BULK EXPORT Completed: ${syncJob}, message: ${syncJob.message}")
    }
}
