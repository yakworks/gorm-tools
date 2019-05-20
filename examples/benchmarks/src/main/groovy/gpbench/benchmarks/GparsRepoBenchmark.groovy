package gpbench.benchmarks

import gorm.tools.repository.RepoUtil
import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

/**
 * Runs batch inserts in parallel using gparse.
 */
@CompileStatic
class GparsRepoBenchmark<T extends GormEntity> extends BaseBatchInsertBenchmark<T> {

    RepositoryApi<T> repo

    GparsRepoBenchmark(Class<T> clazz, String bindingMethod = 'grails') {
        super(clazz, bindingMethod)
        repo = RepoUtil.findRepository(clazz)
    }

    @Override
    def execute() {
        asyncSupport.parallel(cities) { List<Map> list, Map args ->
            repo.batchCreate(list)
            //entityClass.repository.create( row, [validate:validate, dataBinder:dataBinder ])
            //repository.create(row)
            //insertRow(row)
        }
    }
//
//    void insertRow(Map row) {
//        //T c = entityClass.newInstance()
//        T c = repository.getEntityClass().newInstance()
//        c = (T)repository.bind(c, row, null)
//        c.save(failOnError:true, validate:validate)
//    }

}
