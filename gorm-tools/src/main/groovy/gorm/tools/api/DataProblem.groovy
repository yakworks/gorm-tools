/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import groovy.transform.CompileStatic

import org.springframework.dao.InvalidDataAccessResourceUsageException

import gorm.tools.support.MsgSourceResolvable
import gorm.tools.support.SpringMsgKey

/**
 * an extension of the DataRetrievalFailureException that is more performant. fillInStackTrace is overriden to show nothing
 * so it will be faster and consume less memory when thrown.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class DataProblem extends InvalidDataAccessResourceUsageException implements MsgSourceResolvable {

    DataProblem(String msg) {
        super(msg)
        defaultMessage = msg
    }

    DataProblem(String code, Class entityClass) {
        super(code)
        setMessage(
            SpringMsgKey.of(code, [entityClass.simpleName])
        )
    }

}
