package gpbench

import org.springframework.beans.factory.annotation.Value

import gpbench.services.CityFatInsertBenchmarks
import gpbench.services.DataSetup
import grails.core.GrailsApplication
import yakworks.security.spring.SpringSecService

class BootStrap {

    @Value('${runDataBindingBenchmark:false}')
    boolean runDataBindingBenchmark

    @Value('${runBenchmarks:true}')
    boolean runBenchmarks

    CityFatInsertBenchmarks cityFatInsertBenchmarks
    DataSetup dataSetup
    GrailsApplication grailsApplication
    SpringSecService springSecService

    def init = { servletContext ->
        dataSetup.printEnvironment()
        //load base country and region data which is used by all benchmarks
        dataSetup.initBaseData()

        mockAuthentication()

        cityFatInsertBenchmarks.run()
        cityFatInsertBenchmarks.runEvents()
        cityFatInsertBenchmarks.runIssues()

        System.exit(0)
    }

    void mockAuthentication() {
        springSecService.loginAsSystemUser()
    }

}
