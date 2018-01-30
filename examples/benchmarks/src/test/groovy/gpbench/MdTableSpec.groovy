package gpbench

import gorm.tools.Pager
import gorm.tools.testing.GormToolsTest
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

class MdTableSpec extends Specification {

    @Shared Map stats

    void setupSpec() {
        stats = new JsonSlurper().parseText('''{
            "setters static, no associations-CityFatNoTraitsNoAssoc": {
                "benchKey": "setters static, no associations",
                "binderType": "settersStatic",
                "setters/databinding": "0.83s",
                "domainCompile": "static",
                "domainClass": "CityFatNoTraitsNoAssoc",
                "dataSize": 37230,
                "batchSize": 100,
                "poolSize": 5,
                "validate": "2.23s",
                "save batch": "3.43s",
                "save async": "0.97s"
            },
            "setters dynamic-CityFat": {
                "benchKey": "setters dynamic",
                "binderType": "settersDynamic",
                "setters/databinding": "2.07s",
                "domainCompile": "static",
                "domainClass": "CityFat",
                "dataSize": 37230,
                "batchSize": 100,
                "poolSize": 5,
                "validate": "2.67s",
                "save batch": "5.25s",
                "save async": "2.08s"
            },
            "setters static-CityFat": {
                "benchKey": "setters static",
                "binderType": "settersStatic",
                "setters/databinding": "0.98s",
                "domainCompile": "static",
                "domainClass": "CityFat",
                "dataSize": 37230,
                "batchSize": 100,
                "poolSize": 5,
                "validate": "1.74s",
                "save batch": "4.37s",
                "save async": "1.89s"
            },
            "gorm-tools: repository & fast binder-CityFat": {
                "benchKey": "gorm-tools: repository & fast binder",
                "binderType": "fast",
                "setters/databinding": "1.94s",
                "domainCompile": "static",
                "domainClass": "CityFat",
                "dataSize": 37230,
                "batchSize": 100,
                "poolSize": 5,
                "validate": "2.47s",
                "save batch": "5.48s",
                "save async": "1.89s"
            }
        }''')

    }

    def "test default values"() {
        when:
        Map mapLen = [:]

        stats.collect{k,v -> v}[0].keySet().each{
            mapLen[it] = (stats.collect{k,v -> v[it].toString()})*.length().max()
        }
        //see if titles are longer
        mapLen.each{k,v->
            if(k.length() > v) mapLen[k] = k.length()
        }
        List padLeft = ['setters/databinding', 'validate', 'save batch', 'save async']
        String table = "| "

        List cols = ['benchKey', 'domainCompile'] + padLeft
        cols.each{
            def val = padLeft.contains(it) ? it.padLeft(mapLen[it]) : it.padRight(mapLen[it])
            table = table + "$val | "
        }
        table += "\n|"
        cols.each{
            table = table + "-${"".padRight(mapLen[it],'-')}-|"
        }
        //table += "\n| "
        stats.each {k,vmap ->
            table += "\n| "
            cols.each{
                def val = padLeft.contains(it) ? vmap[it].padLeft(mapLen[it]) : vmap[it].padRight(mapLen[it])
                table = table + "$val | "
            }
        }
        //assert mapLen == [benchKey: 12]
//        def maxLen = formatted*.length().max()
//        println formatted.collect { it.padLeft(maxLen) }.join("\n")
//        stats.each {k, statMap ->

        then:
        println table
        table == "foo"
        mapLen == [benchKey: 12]

    }
}
