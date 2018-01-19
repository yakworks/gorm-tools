package gpbench.benchmarks

import gorm.tools.repository.RepoUtil
import gorm.tools.repository.api.RepositoryApi
import gpbench.City
import grails.web.databinding.WebDataBinding
import org.grails.datastore.gorm.GormEntity

class RepoUpdateBenchmark<T extends GormEntity & WebDataBinding> extends BaseUpdateBenchmark<T>{

    RepositoryApi<T> repo

    RepoUpdateBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod, validate)
        repo = RepoUtil.findRepository(clazz)
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
        Map data = getUpdateData(instance)
        data.id = id
        repo.update(data)
    }

}
