/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic

import gorm.tools.databinding.BasicDataBinder
import gorm.tools.job.CoreSyncJobParams
import gorm.tools.repository.model.DataOp
import yakworks.commons.map.Maps
import yakworks.meta.MetaUtils

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is used for Bulk operations.
 * Created at the start of the process, in controller this is created from the params passed the action
 * See BulkableRepo for its primary usage.
 */
@CompileStatic
class BulkImportJobParams extends CoreSyncJobParams{

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
     * (For dataFormat=CSV having attachmentId) CSV header pathDelimiter.
     * Default is ".", pass in "_" for underscore (this is path delimiter for header names, not csv delimiter)
     */
    String headerPathDelimiter

    /**
     * (When attachmentId is set) Format for the data. either CSV or JSON are currently supported.
     */
    String dataFormat

    /**
     * (When attachmentId is set) Control Count of lines or items in file.
     * This is done to ensure file has not been inadvertently truncated.
     */
    Integer controlCount

    /**
     * (For Master/Detail dataFormat=CSV) Header key field that links detail/lines detailLinkField (ArTranLines)
     * to the header record (ArTran). Default is ‘source.sourceId’ but if you have underscores pass in ‘source_sourceId’
     * (when data imported from the file)
     */
    String dataKeyField

    /**
     * (For Master/Detail dataFormat=CSV) The field in the header entity where the collection of lines goes.
     * For ArTran it would be "lines" for example
     */
    String dataDetailField

    /**
     * (For Master/Detail dataFormat=CSV) Field that links back to the dataKeyField in the header file.
     * ‘arTran.sourceId’ for example
     */
    String detailLinkField

    /**
     * (For Master/Detail CSV) The name of the detail lines file. Default is detail.csv
     */
    String detailFilename

    /**
     * force how to store the payload (what was sent)
     */
    Boolean savePayload //= true

    /**
     * force payload to store as file instead of bytes
     */
    Boolean savePayloadAsFile

    /**
     * resulting data (what is returned in response) is always saved but can force it to save to file instead of bytes in column
     */
    Boolean saveDataAsFile //= false


    static BulkImportJobParams withParams(Map params){
        BulkImportJobParams bijParams = new BulkImportJobParams()
        BasicDataBinder.bind(bijParams, params)
        //BeanTools.bind(bijParams, params)
        return bijParams
    }

    // Map asMap(){
    //     Maps.prune(MetaUtils.getProperties(this))
    // }
}
