/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity

import groovy.transform.CompileStatic

import yakworks.rally.activity.model.Activity

/**
 * Static helpers for Activity
 */
@CompileStatic
class ActivityUtil {

    /** Logs info to the Org */
    static Activity log(Long orgId, String text){
        ActivityService.bean().createLog(orgId, text)
    }

}
