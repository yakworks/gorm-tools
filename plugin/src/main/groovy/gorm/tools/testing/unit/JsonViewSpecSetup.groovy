/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.unit

import groovy.transform.CompileDynamic

import org.grails.testing.GrailsUnitTest
import org.grails.web.mapping.DefaultLinkGenerator
import org.grails.web.mapping.UrlMappingsHolderFactoryBean
import org.springframework.web.servlet.i18n.SessionLocaleResolver

import grails.plugin.json.view.JsonViewGrailsPlugin
import grails.testing.spock.OnceBefore

/**
 * Does the setup to make sure beans for json-views are setup
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileDynamic
trait JsonViewSpecSetup implements GrailsUnitTest {

    @OnceBefore
    void setupJsonViewBeans() {
        //setup required beans for JsonViews
        defineBeans {
            grailsLinkGenerator(DefaultLinkGenerator, getConfig()?.grails?.serverURL ?: "http://localhost:8080")
            localeResolver(SessionLocaleResolver)
            grailsUrlMappingsHolder(UrlMappingsHolderFactoryBean){ bean->
                bean.autowire = "byName"
            }
        }
        //calls the doWithSpring in the JsonViewGrailsPlugin class
        defineBeans(new JsonViewGrailsPlugin())
    }

}
