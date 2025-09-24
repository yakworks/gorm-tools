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
import yakworks.commons.map.Maps

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
        Map p = Maps.clone(params)
        //remove the controller and action so we have less noise
        p.remove("controller")
        p.remove("action")
        BulkExportJobArgs bulkArgs = new BulkExportJobArgs()
        BasicDataBinder.bind(bulkArgs, p)
        //BeanTools.bind(bijParams, params)
        //setup queryArgs
        if(p.containsKey("q") || p.containsKey("qSearch") ) {
            bulkArgs.queryArgs = QueryArgs.of(p)
        }
        return bulkArgs
    }
}
