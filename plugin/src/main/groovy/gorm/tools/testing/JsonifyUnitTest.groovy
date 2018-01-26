package gorm.tools.testing

import gorm.tools.json.Jsonify
import grails.buildtestdata.TestData
import grails.plugin.json.view.JsonViewGrailsPlugin
import grails.testing.gorm.DataTest
import grails.testing.spock.OnceBefore
import grails.testing.spring.AutowiredTest
import org.grails.testing.GrailsUnitTest
import org.grails.web.mapping.DefaultLinkGenerator
import org.grails.web.mapping.UrlMappingsHolderFactoryBean
import org.springframework.web.servlet.i18n.SessionLocaleResolver

//@SuppressWarnings(['JUnitPublicNonTestMethod'])
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

    Jsonify.JsonifyResult buildJson(Map args = [:], Class clazz, Map renderArgs = [:]) {
        Object obj = TestData.build(args, clazz)
        return Jsonify.render(obj, renderArgs)
    }

    Jsonify.JsonifyResult buildJson(Map testDataArgs = [:], Map renderArgs = [:]) {
        buildJson(testDataArgs, getEntityClass(), renderArgs)
    }
}
