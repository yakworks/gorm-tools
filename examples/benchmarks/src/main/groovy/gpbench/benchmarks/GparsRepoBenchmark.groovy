package gpbench.benchmarks

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity

import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil

/**
 * Runs batch inserts in parallel using gparse.
 */
@CompileStatic
class GparsRepoBenchmark<T extends GormEntity> extends BaseBatchInsertBenchmark<T> {

    GormRepo<T> repo

    GparsRepoBenchmark(Class<T> clazz, String bindingMethod = 'grails') {
        super(clazz, bindingMethod)
        repo = RepoUtil.findRepo(clazz)
    }

    @Override
    def execute() {
        parallelTools.each(cities) { List<Map> list->
            repo.batchTrx(list) { Map item ->
                repo.doCreate(item, [:])
            }
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
