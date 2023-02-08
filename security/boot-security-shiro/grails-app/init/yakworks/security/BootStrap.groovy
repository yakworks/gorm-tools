/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import org.grails.datastore.mapping.reflect.ClassUtils

import grails.core.GrailsApplication
import grails.util.Metadata

class BootStrap {

    GrailsApplication grailsApplication

    def init = { servletContext ->
        def appName = Metadata.current.getApplicationName()
        //onyl run for this, it will try to run this for projects that depend on this in examples so dont
        if(appName == "boot-security"){
            def sd = BootStrap.classLoader.loadClass("yakworks.rally.testing.RallySeedData")
            sd.init()
            sd.fullMonty(50)
        }
    }
}
