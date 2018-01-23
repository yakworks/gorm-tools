package gpbench.benchmarks.update

import gpbench.City
import grails.web.databinding.WebDataBinding
import org.grails.datastore.gorm.GormEntity
import org.springframework.jdbc.core.JdbcTemplate

class GparsBaseLineUpdateBenchmark<T extends GormEntity & WebDataBinding> extends BaseUpdateBenchmark<T>{

    JdbcTemplate jdbcTemplate

    GparsBaseLineUpdateBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod, validate)
    }

    @Override
    protected execute() {
        List<Long> all = City.executeQuery("select id from ${domainClass.getSimpleName()}".toString())
        List<List<Long>> batches = all.collate(batchSize)

        asyncBatchSupport.parallelBatch(batches){Long id, Map args ->
            updateRow(id)
        }
    }


    void updateRow(Long id) {
        def instance = domainClass.get(id)
        instance.properties = getUpdateData(instance)
        instance.save()
    }

}
