/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic

import gorm.tools.databinding.BasicDataBinder
import gorm.tools.job.DataLayout
import gorm.tools.job.SyncJobArgs
import gorm.tools.mango.api.QueryArgs

/**
 * Bulk Export Args
 */
@CompileStatic
class BulkExportJobArgs extends SyncJobArgs {
    public static final JOB_TYPE = 'bulk.export'

    String jobType = JOB_TYPE

    /**
     * The entity class name this is for
     */
    String entityClassName

    Class entityClass

    static BulkExportJobArgs fromParams(Map params){
        BulkExportJobArgs bulkArgs = new BulkExportJobArgs()
        BasicDataBinder.bind(bulkArgs, params)
        //BeanTools.bind(bijParams, params)
        //setup queryArgs
        if(params.containsKey("q") || params.containsKey("qSearch") ) {
            bulkArgs.queryArgs = QueryArgs.of(params)
        }
        return bulkArgs
    }
}
