/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic

import gorm.tools.databinding.BasicDataBinder
import gorm.tools.job.SyncJobArgs

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is used for Bulk operations.
 * Created at the start of the process, in controller this is created from the params passed the action
 * See BulkableRepo for its primary usage.
 */
@CompileStatic
class BulkExportJobArgs extends SyncJobArgs {
    public static final JOB_TYPE = 'bulk.export'

    String jobType = JOB_TYPE

    /**
     * The entity class name this is for
     */
    String entityClassName

    static BulkExportJobArgs withParams(Map params){
        BulkExportJobArgs bulkParams = new BulkExportJobArgs()
        BasicDataBinder.bind(bulkParams, params)
        //BeanTools.bind(bijParams, params)
        return bulkParams
    }
}
