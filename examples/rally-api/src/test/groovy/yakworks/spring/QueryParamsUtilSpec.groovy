package yakworks.spring

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.gorm.api.ApiConfig
import yakworks.gorm.api.ApiUtils
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * sanity checking that @ConfigurationProperties works
 */
class QueryParamsUtilSpec extends Specification { //implements GrailsAppUnitTest {

    Map<String, String[]> parseQuery(String v){
        QueryParamsUtil.parseQueryString(v)
    }

    Map<String, String[]> toStringMap(v){
        v as Map<String, String[]>
    }

    def "parse query params"() {
        expect:
        parseQuery("") == [:]
        parseQuery(null) == [:]

        parseQuery("name=abc.zip") == [name: ["abc.zip"]] //as Map<String, String[]>
        parseQuery("na%20me=ab%20c.zip") == ["na me": ["ab c.zip"]]
        parseQuery("name=abc.zip&size=111") == [name:["abc.zip"], size:["111"]]
        parseQuery("name=") == [name: [""]]
        parseQuery("name=&size=10") == [name:[""], size:["10"]]
        parseQuery("name=test.zip&size=") == [name:["test.zip"], size:[""]]
        parseQuery("name=test.zip&size") == [name: ["test.zip"], size:[""]]

        when:
        Map params = parseQuery("includes=custNum,refnum,amount,%20origAmount,%20tranDate,%20tranType.name,ponum&max=1000000000&format=csv&q={%22state%22:[0],%22member%22:{%22division%22:{%22num%22:%22CO%22}}")

        then:
        noExceptionThrown()
        params.size() == 4
        params.keySet().containsAll(["includes", "max", "format", "q"])
        params.includes == ["custNum,refnum,amount, origAmount, tranDate, tranType.name,ponum"]
        params.max == ["1000000000"]
        params.q == ['{"state":[0],"member":{"division":{"num":"CO"}}']
    }

}
