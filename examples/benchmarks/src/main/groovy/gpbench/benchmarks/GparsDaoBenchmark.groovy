package gpbench.benchmarks

import gorm.tools.repository.RepoUtil
import gorm.tools.repository.RepositoryApi
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

/**
 * Runs batch inserts in parallel using gparse.
 */
@CompileStatic
class GparsDaoBenchmark<T extends GormEntity> extends BaseBatchInsertBenchmark<T> {

    RepositoryApi<T> repo

    GparsDaoBenchmark(Class<T> clazz, String bindingMethod = 'grails') {
        super(clazz, bindingMethod)
        repo = RepoUtil.findRepository(clazz)
    }

    @Override
    def execute() {
        asyncBatchSupport.parallel(cities) { List<Map> list, Map args ->
            repo.batchCreate(list)
            //domainClass.repository.create( row, [validate:validate, dataBinder:dataBinder ])
            //repository.create(row)
            //insertRow(row)
        }
    }
//
//    void insertRow(Map row) {
//        //T c = domainClass.newInstance()
//        T c = repository.getDomainClass().newInstance()
//        c = (T)repository.bind(c, row, null)
//        c.save(failOnError:true, validate:validate)
//    }

}
