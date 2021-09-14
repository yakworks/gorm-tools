/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import grails.gorm.DetachedCriteria

/**
 * Get rid of those pesky Entities....
 * Seriously though, the DetachedCriteria deleteAll doesn't work for map or noSql. So this allows you to replace it.
 */
@Slf4j
@CompileStatic
class CriteriaRemover {

    void deleteAll(DetachedCriteria crit) {
        crit.deleteAll()
    }

}
