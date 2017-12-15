package gorm.tools.dao

import gorm.tools.WithTrx
import groovy.transform.CompileStatic

@CompileStatic
trait GormBatchDao<D> implements WithTrx, DaoApi<D>{

    /**
     * Transactional, Iterates over list and runs closure for each item
     */
    void batchTrx(List list, Closure closure) {
        withTrx {
            for (Object item : list) {
                closure(item)
            }
            DaoUtil.flushAndClear()
        }
    }

    void batchPersist(Map args = [:], List<D> list) {
        batchTrx(list){ D item ->
            doPersist(item, args)
        }
    }

    void batchCreate(Map args = [:], List<Map> list) {
        batchTrx(list){ Map item ->
            doCreate(item, args)
        }
    }

    void batchUpdate(Map args = [:], List<Map> list) {
        batchTrx(list){ Map item ->
            doUpdate(item, args)
        }
    }

    void batchRemove(Map args = [:], List list) {
        batchTrx(list){ Serializable item ->
            removeById(item, args)
        }
    }

}
