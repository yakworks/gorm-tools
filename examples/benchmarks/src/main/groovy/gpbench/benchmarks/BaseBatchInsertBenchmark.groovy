package gpbench.benchmarks

import groovy.transform.CompileDynamic

import gorm.tools.async.ParallelTools
import gorm.tools.repository.RepoUtil
import gpbench.basic.CityBasic
import gpbench.helpers.JsonReader
import gpbench.helpers.RecordsLoader
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService

//@CompileStatic
abstract class BaseBatchInsertBenchmark<T> extends AbstractBenchmark {
    int poolSize
    int batchSize

    ParallelTools parallelTools

    JsonReader jsonReader

    SpringSecurityService springSecurityService

    Class<T> domainClass = CityBasic

    boolean useDatabinding = true //use default grails databinding
    boolean validate = true
    String dataBinder = 'grails' // can be copy or setter

    List<List<Map>> cities
    int cityListSize = 37230
    int repeatedCityTimes = 10

    BaseBatchInsertBenchmark(boolean useDatabinding) {
        this.useDatabinding = useDatabinding
        if (!useDatabinding) dataBinder = 'copy'
    }

    BaseBatchInsertBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        if (bindingMethod != 'grails') useDatabinding = false
        this.validate = validate
        this.dataBinder = bindingMethod
        domainClass = clazz
    }

    void setup() {
        assert springSecurityService.principal.id != null
        assert springSecurityService.principal.id == 1
        assert domainClass.count() == 0

        RecordsLoader recordsLoader = jsonReader //useDatabinding ? csvReader : jsonReader
        List cityfull = recordsLoader.read("City")
        List repeatedCity = []
        (1..repeatedCityTimes).each { i ->
            repeatedCity = repeatedCity + cityfull
        }
        //println repeatedCity.size()
        cities = repeatedCity.collate(batchSize)
        //println cities.size()
    }

    @Transactional
    @CompileDynamic
    void cleanup() {
        assert domainClass.count() == cityListSize * repeatedCityTimes//345000 //37230
        domainClass.executeUpdate("delete from ${domainClass.getSimpleName()}".toString())
        RepoUtil.flushAndClear()
    }

    @Override
    String getDescription() {
        String validateDesc = validate ? "" : ", validation: ${validate}"
        return "${this.getClass().simpleName}<${domainClass.simpleName}> [ dataBinder: ${dataBinder} ${validateDesc}]"
    }
}
