/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.api

import groovy.transform.CompileStatic

import org.springframework.transaction.TransactionStatus

import gorm.tools.transaction.WithTrx

/**
 * A trait add batch processing and "mass" updating methods to a Repository
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.x
 */
@CompileStatic
trait GormBatchRepo<D> implements WithTrx, RepositoryApi<D> {

    /**
     * Transactional, Iterates over list and runs closure for each item
     */
    void batchTrx(List list, Closure closure) {
        withTrx { TransactionStatus status ->
            for (Object item : list) {
                closure(item)
            }
            flushAndClear(status)
        }
    }

    void batchPersist(Map args = [:], List<D> list) {
        batchTrx(list) { D item ->
            doPersist(args, item)
        }
    }

    void batchCreate(Map args = [:], List<Map> list) {
        batchTrx(list) { Map item ->
            doCreate(args, item)
        }
    }

    void batchUpdate(Map args = [:], List<Map> list) {
        batchTrx(list) { Map item ->
            doUpdate(args, item)
        }
    }

    void batchRemove(Map args = [:], List list) {
        batchTrx(list) { Serializable item ->
            removeById(args, item)
        }
    }

}
