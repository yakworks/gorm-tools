package gpbench.benchmarks.update

import java.util.concurrent.atomic.AtomicInteger

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.async.AsyncArgs
import gorm.tools.databinding.EntityMapBinder
import gpbench.model.basic.CityBasic

@CompileStatic
class GparsBaseLineUpdateBenchmark<T> extends BaseUpdateBenchmark<T>{

    EntityMapBinder entityMapBinder

    GparsBaseLineUpdateBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod, validate)
    }

    @Override
    protected execute() {
        List all = CityBasic.executeQuery("select id from ${domainClass.getSimpleName()}".toString()) as List<Long>
        List<List<Long>> batches = all.collate(batchSize)
        AtomicInteger at = new AtomicInteger(-1)

        def sliceClosure = parallelTools.sliceClosure { Long id ->
            updateRow(id, citiesUpdated[at.incrementAndGet()])
        }
        parallelTools.each(AsyncArgs.transactional(), cities, sliceClosure)
    }


    @CompileDynamic
    void updateRow(Long id, Map row) {
        def instance = domainClass.get(id)
        entityMapBinder.bind(instance, row)
        instance.save()
    }

}
