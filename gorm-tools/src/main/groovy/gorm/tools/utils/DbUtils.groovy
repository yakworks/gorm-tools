/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils

import groovy.transform.CompileStatic

@CompileStatic
class DbUtils {

    /**
     * Builds an hql condition as "column is null " or "column = :column" based on the value.
     */
    static String nullSafeHqlCondition(String column, Object value) {
        if (value == null) {
            return "${column} is null "
        } else {
            return "${column} = :${column}"
        }
    }

    /**
     * Builds an hql condition as "column is null " or "column = :column" based on the value.
     */
    static String nullSafeSqlCondition(String column, Object value) {
        String condition
        if (value == null) {
            condition = "${column} is null "
        } else {
            if (value instanceof String) {
                condition = "${column} = '$value'"
            } else {
                condition = "${column} = $value"
            }
        }
        return condition
    }

}
