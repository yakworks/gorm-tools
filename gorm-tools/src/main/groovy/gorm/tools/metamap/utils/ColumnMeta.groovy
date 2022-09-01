/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap.utils

import groovy.transform.CompileStatic

/**
 * Useful for info needed when building a table based data. Excel, CSV, Jasper report, etc...
 * Stores basic information about a property column.
 */
@CompileStatic
class ColumnMeta implements Serializable {

    ColumnMeta() {}

    ColumnMeta(String property) {
        this.property = property
    }

    /**
     * the property or nested path for object such as "amount" or "customer.name"
     */
    String property

    /**
     * the java long qualified class name
     * ex: java.lang.String, java.math.BigDecimal etc...
     */
    Class<?> typeClass //= Object

    /**
     * the java long qualified class name
     * ex: java.lang.String, java.math.BigDecimal etc...
     */
    String typeClassName

    /**
     * the display label for this property
     */
    String title

    /**
     * override the format to use special
     */
    String format

    /**
     * override on how to align this. left, right, middle
     */
    String align

    /**
     * hide this by default
     */
    Boolean hide

    /**
     * the width to override
     */
    Integer width

    /**
     * a builder reference that can be used, depending on the implementation
     */
    Object builder

    Boolean isBooleanType() {
        if (typeClass == Boolean || typeClassName == 'java.lang.Boolean') {
            return true
        }
    }

}
