/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import yakworks.commons.lang.EnumUtils

/**
 * Statics for the operations
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@SuppressWarnings(['FieldName', 'ConfusingMethodName']) //codenarc doesn't like the names we use to make this builder clean
@CompileStatic
@Slf4j
class MangoOps {

    public static final String CRITERIA = 'criteria'
    public static final String SORT = '$sort'
    public static final String Q = '$q'
    public static final String PROJECTIONS = '$projections'
    public static final String SELECT = '$select'
    // if passing q but also want the fuzzy search then nest it in this field
    public static final String QSEARCH = '$qSearch'

    @CompileStatic
    static enum CompareOp {
        $gt, $eq, $gte, $lt, $lte, $ne, $not, $ilike, $like, $in, $inList

        //the op is the value without the $
        private final String op //private for security
        String getOp(){ return op }

        CompareOp() {
            this.op = name().substring(1) //remove $
        }
    }

    @CompileStatic
    static enum PropertyOp {
        $gtf('gtProperty'),
        $gtef('geProperty'),
        $ltf('ltProperty'),
        $ltef('leProperty'),
        $eqf('eqProperty'),
        $nef('neProperty')

        private final String op //private for security
        String getOp(){ return op }

        PropertyOp(String op) {
            this.op = op
        }
    }

    @CompileStatic
    static enum OverrideOp {
        $between('between'),
        $nin('notIn')

        private final String op
        String getOp(){ return op }

        OverrideOp(String op) {
            this.op = op
        }
    }

    @CompileStatic
    static enum JunctionOp {
        $and, $or, $not

        private final String op //private for security
        String getOp(){ return op }

        JunctionOp() {
            this.op = name().substring(1) //remove $
        }
    }

    @CompileStatic
    static enum SubQueryOp {
        $exists

        private final String op //private for security
        String getOp(){ return op }

        SubQueryOp() {
            this.op = name().substring(1) //remove $
        }
    }

    @CompileStatic
    static enum ExistOp {
        $isNull, $isNotNull

        private final String op
        String getOp(){ return op }

        ExistOp() {
            this.op = name().substring(1) //remove $
        }
    }

    @CompileStatic
    static enum ProjectionOp {
        $sum, $count, $group, $avg, $countDistinct, $distinct

        private final String op
        String getOp(){ return op }

        ProjectionOp() {
            this.op = name().substring(1) //remove $
        }
    }

    static boolean isValidOp(String key) {
        EnumUtils.isValidEnum(ExistOp, key) ||
            EnumUtils.isValidEnum(PropertyOp, key) ||
            EnumUtils.isValidEnum(CompareOp, key)
    }
}
