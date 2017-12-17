package gpbench.benchmarks

import gorm.tools.repository.RepoUtil
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import org.grails.datastore.gorm.GormEntity

/**
 * Calls external script for each row. The script does the insert.
 */
@CompileStatic
class RxJavaBenchmark<T extends GormEntity> extends GparsBaselineBenchmark<T> {

    RxJavaBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod, validate)
    }

    @Override
    def execute() {
        Flowable<List<Map>> stream = Flowable.fromIterable(cities)
        stream.parallel().runOn(Schedulers.computation()).map({ List<Map> batch ->
            //println "${batch.size()} Thread : " + Thread.currentThread().name
            insertBatch(batch)
            return true
        }).sequential().blockingForEach({})
    }

    @Transactional
    //@CompileStatic(TypeCheckingMode.SKIP)
    void insertBatch(List<Map> batch) {
        for (Map row : batch) {
            insertRow(row)
        }

        RepoUtil.flushAndClear()
    }

}
