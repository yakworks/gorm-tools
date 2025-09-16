/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic
import groovy.transform.ToString

import gorm.tools.databinding.BasicDataBinder
import gorm.tools.job.DataLayout
import gorm.tools.job.SyncJobArgs
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.model.DataOp
import yakworks.commons.map.Maps
import yakworks.etl.DataMimeTypes

/**
 * DTO for the BulkImport process
 */
@ToString(includeSuperProperties = true, includeNames = true,
    includes = ['jobType', 'jobId', 'jobType', 'op', 'source', 'sourceId', 'entityClassName'])
@CompileStatic
class BulkImportJobArgs extends SyncJobArgs {
    public static final JOB_TYPE = 'bulk.import'

    String jobType = JOB_TYPE

    //override default
    DataLayout dataLayout = DataLayout.Result

    /**
     * the operation to perform, Used in bulk and limited to add, update and upsert right now.
     */
    DataOp op

    /**
     * Id of attachment record for CSV and JSON. Attachment must be a zip file.
     */
    Long attachmentId

    /**
     * (when attachmentId is set) the name of the payload file in the zip, defaults to data.csv
     */
    String payloadFilename

    /**
     * (For payloadFormat=CSV with attachmentId) CSV header pathDelimiter.
     * Default is ".", pass in "_" for underscore (this is path delimiter for header names, not csv delimiter)
     */
    String headerPathDelimiter

    /**
     * (When attachmentId/payloadId is set) Format for the data. either CSV or JSON are currently supported.
     */
    DataMimeTypes payloadFormat

    /**
     * (When attachmentId is set) Control Count of lines or items in file.
     * This is done to ensure file has not been inadvertently truncated.
     */
    Integer controlCount

    /**
     * The entity class name this is for
     */
    String entityClassName

    Class entityClass

    /**
     * the args, such as flush:true etc.., to pass down to the repo methods
     * Helpful for bindId when bulk importing rows that have id already.
     */
    PersistArgs persistArgs

    /**
     * (For Master/Detail payloadFormat=CSV)
     * Header key field that links detail/lines detailLinkField (ArTranLines)
     * to the header record (ArTran). Default is ‘source.sourceId’ but if you have underscores pass in ‘source_sourceId’
     * (when data imported from the file)
     */
    String dataKeyField

    /**
     * (For Master/Detail payloadFormat=CSV)
     * The field in the header entity where the collection of lines goes.
     * For ArTran it would be "lines" for example
     */
    String dataDetailField

    /**
     * (For Master/Detail payloadFormat=CSV)
     * Field that links back to the dataKeyField in the header file.
     * ‘arTran.sourceId’ for example
     */
    String detailLinkField

    /**
     * (For Master/Detail CSV)
     * The name of the detail/lines file in the zip. Default is detail.csv?
     */
    String detailFilename

    static BulkImportJobArgs fromParams(Map params){
        Map p = Maps.clone(params)
        //remove the controller and action so we have less noise
        p.remove("controller")
        p.remove("action")
        BulkImportJobArgs bijParams = new BulkImportJobArgs()
        BasicDataBinder.bind(bijParams, p)
        //put a full copy as is into the queryParams
        bijParams.queryParams = p
        //XXX remove this once we know its not being used
        if(params.dataFilename) bijParams.payloadFilename = params.dataFilename
        return bijParams
    }

}
