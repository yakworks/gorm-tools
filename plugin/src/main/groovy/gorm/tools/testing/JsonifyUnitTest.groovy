package gorm.tools.testing

import gorm.tools.json.Jsonify
import grails.buildtestdata.TestData
import grails.plugin.json.view.JsonViewGrailsPlugin
import grails.testing.spock.OnceBefore
import groovy.transform.CompileDynamic
import org.grails.testing.GrailsUnitTest
import org.grails.web.mapping.DefaultLinkGenerator
import org.grails.web.mapping.UrlMappingsHolderFactoryBean
import org.springframework.web.servlet.i18n.SessionLocaleResolver

/**
 * Uses build-test-data plugin and Jsonify wrapper to json-view to build. Does the setup to make sure beans
 * for json-views are s
 */
@CompileDynamic
trait JsonifyUnitTest implements GrailsUnitTest {

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

    /**
     * Uses the build-test-data plugin to first build the entity with data and then
     * @param args
     * @param entityClass
     * @param renderArgs passed to {@link Jsonify} and json-views.
     * @return use return.json to get the map
     */
    Jsonify.JsonifyResult buildJson(Map args = [:], Class entityClass, Map renderArgs = [:]) {
        Object obj = TestData.build(args, entityClass)
        return Jsonify.render(obj, renderArgs)
    }

    /** see {@link TestData#build} for args and {@link Jsonify.} for renderArgs */
    Map buildMap(Map args = [:], Class entityClass, Map renderArgs = [:]) {
        buildJson(args, entityClass, renderArgs).json as Map
    }

}
