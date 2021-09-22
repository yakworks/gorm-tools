package gpbench.benchmarks.update

import gorm.tools.async.ParallelConfig
import gpbench.model.basic.CityBasic
import gorm.tools.databinding.EntityMapBinder

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import java.util.concurrent.atomic.AtomicInteger

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
        parallelTools.each(ParallelConfig.transactional(), cities, sliceClosure)
    }


    @CompileDynamic
    void updateRow(Long id, Map row) {
        def instance = domainClass.get(id)
        entityMapBinder.bind(instance, row)
        instance.save()
    }

}
