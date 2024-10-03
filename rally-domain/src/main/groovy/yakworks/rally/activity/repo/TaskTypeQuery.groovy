/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.repo


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import gorm.tools.mango.DefaultQueryService
import yakworks.rally.activity.model.Task

/**
 * This is here just to test that having multiple QueryServices will work
 * and not blow up on injection
 */
@Service @Lazy
@CompileStatic
@Slf4j
class TaskTypeQuery extends DefaultQueryService<Task> {

    TaskTypeQuery() {
        super(Task)
    }

}
