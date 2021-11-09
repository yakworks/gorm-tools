/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.errors

import groovy.transform.CompileStatic

import org.springframework.dao.DataRetrievalFailureException
import org.springframework.dao.InvalidDataAccessResourceUsageException

import gorm.tools.support.MsgKey
import gorm.tools.support.MsgSourceResolvable

/**
 * an extension of the DataRetrievalFailureException that is more performant. fillInStackTrace is overriden to show nothing
 * so it will be faster and consume less memory when thrown.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class DataException extends InvalidDataAccessResourceUsageException implements MsgSourceResolvable {

    DataException(String msg) {
        super(msg)
        defaultMessage = msg
    }

    DataException(String code, Class entityClass) {
        super(code)
        setMessage(
            MsgKey.of(code, [entityClass.simpleName])
        )
    }

}
