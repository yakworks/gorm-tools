package gpbench.benchmarks

import gorm.tools.databinding.GormMapBinder
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity

/**
 * Baseline benchmark with grails out of the box
 */

@CompileStatic
class GparsBaselineBenchmark<T extends GormEntity & WebDataBinding> extends BaseBatchInsertBenchmark<T> {

    GormMapBinder gormMapBinder

    GparsBaselineBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod, validate)
    }

    @Override
    def execute() {
        asyncBatchSupport.parallel(cities) { Map row, Map zargs ->
            //println "insertingRow $row"
            insertRow(row)
        }
    }

    void insertRow(Map row) {
        T c = domainClass.newInstance()
        if (dataBinder == 'grails') {
            bindGrails(c, row)
        } else {
            gormMapBinder.bind(c, row)
        }
        c.save(failOnError: true, validate: validate)
    }

    @CompileDynamic
    void bindGrails(T entity, Map row) {
        entity.properties = row
    }

}
