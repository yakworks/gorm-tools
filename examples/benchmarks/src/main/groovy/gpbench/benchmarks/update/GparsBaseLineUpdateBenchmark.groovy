package gpbench.benchmarks.update

import gpbench.basic.CityBasic
import gorm.tools.databinding.EntityMapBinder
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

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
        asyncSupport.parallel(cities) { List batch ->
            asyncSupport.batchTrx(batch) {Long id ->
                updateRow(id, citiesUpdated[at.incrementAndGet()])
            }
        }
    }


    @CompileDynamic
    void updateRow(Long id, Map row) {
        def instance = domainClass.get(id)
        entityMapBinder.bind(instance, row)
        instance.save()
    }

}
