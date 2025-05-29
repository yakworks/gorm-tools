/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic

import gorm.tools.databinding.BasicDataBinder
import gorm.tools.job.CoreSyncJobParams
import gorm.tools.repository.model.DataOp

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is used for Bulk operations.
 * Created at the start of the process, in controller this is created from the params passed the action
 * See BulkableRepo for its primary usage.
 */
@CompileStatic
class BulkImportJobParams extends CoreSyncJobParams {

    /**
     * the operation to perform, Used in bulk and limited to add, update and upsert right now.
     */
    DataOp op

    /**
     * Id of attachment record for CSV and JSON. Attachment must be a zip file.
     */
    Long attachmentId

    /**
     * (when attachmentId is set) the name of the data file in the zip, defaults to data.csv
     */
    String dataFilename

    /**
     * (For payloadFormat=CSV with attachmentId) CSV header pathDelimiter.
     * Default is ".", pass in "_" for underscore (this is path delimiter for header names, not csv delimiter)
     */
    String headerPathDelimiter

    /**
     * (When attachmentId is set) Format for the data. either CSV or JSON are currently supported.
     */
    PayloadFormat payloadFormat

    static enum PayloadFormat { csv, json }

    /**
     * (When attachmentId is set) Control Count of lines or items in file.
     * This is done to ensure file has not been inadvertently truncated.
     */
    Integer controlCount

    /**
     * The entity class name this is for
     */
    String entityClassName

    static BulkImportJobParams withParams(Map params){
        BulkImportJobParams bijParams = new BulkImportJobParams()
        BasicDataBinder.bind(bijParams, params)
        bijParams.queryParams = params
        return bijParams
    }

}
