package gpbench.benchmarks.update

import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.RepositoryApi
import gpbench.basic.CityBasic
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

import java.util.concurrent.atomic.AtomicInteger

@CompileStatic
class RepoUpdateBenchmark<T> extends BaseUpdateBenchmark<T>{

    RepositoryApi<T> repo

    RepoUpdateBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod, validate)
        repo = RepoUtil.findRepo(clazz)
    }

    @Override
    protected execute() {
        List all = CityBasic.executeQuery("select id from ${domainClass.getSimpleName()}".toString()) as List<Long>
        List<List<Long>> batches = all.collate(batchSize)
        AtomicInteger at = new AtomicInteger(-1)
        asyncSupport.parallelBatch(batches){Long id, Map args ->
            updateRow(id, citiesUpdated[at.incrementAndGet()])
        }
    }

    @CompileDynamic
    void updateRow(Long id, Map row) {
        row.id = id
        repo.update(row)
    }

}
