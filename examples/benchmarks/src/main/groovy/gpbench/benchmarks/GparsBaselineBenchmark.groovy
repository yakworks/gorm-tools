package gpbench.benchmarks

import gorm.tools.databinding.FastBinder
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileStatic
import groovy.transform.CompileDynamic
import org.grails.datastore.gorm.GormEntity

/**
 * Baseline benchmark with grails out of the box
 */

@CompileStatic
class GparsBaselineBenchmark<T extends GormEntity & WebDataBinding> extends BaseBatchInsertBenchmark<T> {

    FastBinder fastBinder

    GparsBaselineBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod,validate)
    }

    @Override
    def execute() {
        gparsBatchService.eachParallel(cities){ Map row, Map zargs ->
            //println "insertingRow $row"
            insertRow(row)
        }
    }

    void insertRow(Map row) {
        T c = domainClass.newInstance()
        if (dataBinder == 'grails') {
            bindGrails(c, row)
        }
        else {
            fastBinder.bind(c, row)
        }
        c.save(failOnError:true, validate:validate)
    }

    @CompileDynamic
    void bindGrails(T entity, Map row){
        entity.properties = row
    }

}
