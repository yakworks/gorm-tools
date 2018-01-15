package gpbench.benchmarks

import gorm.tools.databinding.BindAction
import gorm.tools.databinding.EntityMapBinder
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

/**
 * Baseline benchmark with grails out of the box
 */

@CompileStatic
class GparsBaselineBenchmark<T extends GormEntity & WebDataBinding> extends BaseBatchInsertBenchmark<T> {

    EntityMapBinder entityMapBinder

    GparsBaselineBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod, validate)
    }

    @Override
    def execute() {
        asyncBatchSupport.parallelBatch(cities) { Map row, Map zargs ->
            //println "insertingRow $row"
            insertRow(row)
        }
    }

    void insertRow(Map row) {
        T c = domainClass.newInstance()
        if (dataBinder == 'grails') {
            bindGrails(c, row)
        } else {
            entityMapBinder.bind(c, row)
        }
        c.save(failOnError: true, validate: validate)
    }

    @CompileDynamic
    void bindGrails(T entity, Map row) {
        entity.properties = row
    }

}
