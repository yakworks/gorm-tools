package gpbench

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

    BenchmarkRunnerService benchmarkRunnerService
    BenchmarkDatabindingService benchmarkDatabindingService

    def init = { servletContext ->
        mockAuthentication()
        if(runBenchmarks && runDataBindingBenchmark){
            benchmarkDatabindingService.runFast()
            benchmarkDatabindingService.runFast(true)
        }
        //loaderNoPersistService.runFileLoad()
        //loaderNoPersistService.run()
        if(runBenchmarks)
            benchmarkRunnerService.runBenchMarks()
    }

    void mockAuthentication() {
        //makes sure that each spawned thread has the access to the logged in user
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)

        GrailsUser grailsUser = new GrailsUser("test", "test", true,
            true, false, true, AuthorityUtils.createAuthorityList('ROLE_ADMIN'), 1 as Long)
        SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(grailsUser, "test", AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
    }
}
