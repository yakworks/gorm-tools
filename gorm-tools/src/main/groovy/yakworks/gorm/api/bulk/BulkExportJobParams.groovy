/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic

import gorm.tools.job.CoreSyncJobParams
import yakworks.commons.map.Maps
import yakworks.meta.MetaUtils

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is used for Bulk operations.
 * Created at the start of the process, in controller this is created from the params passed the action
 * See BulkableRepo for its primary usage.
 */
@CompileStatic
class BulkExportJobParams extends CoreSyncJobParams{

    /**
     * (When attachmentId is set) Format for the data. either CSV or JSON are currently supported.
     */
    String dataFormat

}
