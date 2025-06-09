/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic

import gorm.tools.databinding.BasicDataBinder

/**
 * Used for multiple CSV files in zip.
 * being the master and the other being the detail.
 * For example, Invoices Header in data.csv and the lines for the invoice in the detail.csv
 */
@CompileStatic
class BulkMasterDetailJobParams extends BulkImportJobParams {

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

    static BulkMasterDetailJobParams withParams(Map params){
        BulkMasterDetailJobParams bijParams = new BulkMasterDetailJobParams()
        BasicDataBinder.bind(bijParams, params)
        return bijParams
    }

}
