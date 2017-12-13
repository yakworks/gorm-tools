package gpbench.benchmarks

import gorm.tools.dao.DaoApi
import gorm.tools.dao.DaoUtil
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

/**
 * Runs batch inserts in parallel using gparse.
 */
@CompileStatic
class GparsDaoBenchmark<T extends GormEntity> extends BaseBatchInsertBenchmark<T> {

    //FastBinder fastBinder

    DaoApi<T> dao

    GparsDaoBenchmark(Class<T> clazz, String bindingMethod = 'grails') {
        super(clazz, bindingMethod)
        dao = DaoUtil.findDao(clazz)
    }

    @Override
    def execute() {
        asyncBatchSupport.parallel(cities) { Map row, Map cargs ->
            //domainClass.dao.create( row, [validate:validate, dataBinder:dataBinder ])
            dao.doCreate(row)
            //insertRow(row)
        }
    }
//
//    void insertRow(Map row) {
//        //T c = domainClass.newInstance()
//        T c = dao.getDomainClass().newInstance()
//        c = (T)dao.bind(c, row, null)
//        c.save(failOnError:true, validate:validate)
//    }

}
