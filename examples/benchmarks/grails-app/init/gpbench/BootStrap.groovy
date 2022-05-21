package gpbench

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder

import gpbench.services.CityFatInsertBenchmarks
import gpbench.services.DataSetup
import grails.core.GrailsApplication
import grails.plugin.springsecurity.userdetails.GrailsUser

class BootStrap {

    @Value('${runDataBindingBenchmark:false}')
    boolean runDataBindingBenchmark

    @Value('${runBenchmarks:true}')
    boolean runBenchmarks

    CityFatInsertBenchmarks cityFatInsertBenchmarks
    DataSetup dataSetup
    GrailsApplication grailsApplication

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
        //makes sure that each spawned thread has the access to the logged in user
        // SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)

        GrailsUser grailsUser = new GrailsUser("test", "test", true,
            true, false, true, AuthorityUtils.createAuthorityList('ADMIN'), 1 as Long)
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(grailsUser, "test", AuthorityUtils.createAuthorityList('ADMIN'))
    }

}
