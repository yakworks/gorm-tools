package foobar

import org.grails.io.support.GrailsResourceUtils
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

/**
 * Created by basejump on 9/29/16.
 */
class TenantViewResourceLoader implements ResourceLoader,ApplicationContextAware  {

    ResourceLoader internalResourceLoader
    ApplicationContext applicationContext

    static ThreadLocal<String> currentTenant = new ThreadLocal<String>(){
        protected synchronized String initialValue() {
            //hard code for now until we get the multi-tenant wired in
            return "xxx";
        }
    };

    @Override
    Resource getResource(String uri) {
        String tpath = GrailsResourceUtils.appendPiecesForUri("file:view-templates/",currentTenant.get(),uri)
        def gpr = applicationContext.groovyPageResourceLoader
        if(gpr){
            gpr.getResource(tpath)
        }else{
            def res = applicationContext.getResource(tpath)
            println res
            return res
        }
    }

    @Override
    ClassLoader getClassLoader() {
        return null
    }

}
