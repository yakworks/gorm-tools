package gpbench

import grails.core.GrailsApplication
import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

class BootStrap {

    @Value('${runDataBindingBenchmark:false}')
    boolean runDataBindingBenchmark

    @Value('${runBenchmarks:true}')
    boolean runBenchmarks

    DeprecatedRunnerService deprecatedRunnerService
    CityFatInsertBenchmarks cityFatInsertBenchmarks
    DataSetup dataSetup
    GrailsApplication grailsApplication

    def init = { servletContext ->
        dataSetup.printEnvironment()
        //load base country and city data which is used by all benchmarks
        dataSetup.initBaseData()

        mockAuthentication()

        cityFatInsertBenchmarks.run()
        cityFatInsertBenchmarks.runEvents()

//        if(runBenchmarks)
//            benchmarkRunnerService.runBenchMarks()

        System.exit(0)
    }

    void mockAuthentication() {
        //makes sure that each spawned thread has the access to the logged in user
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)

        GrailsUser grailsUser = new GrailsUser("test", "test", true,
            true, false, true, AuthorityUtils.createAuthorityList('ROLE_ADMIN'), 1 as Long)
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(grailsUser, "test", AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
    }

}
