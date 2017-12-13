package gpbench.helpers

import grails.core.GrailsApplication
import grails.plugins.csv.CSVMapReader
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

//import org.springframework.mock.web.MockHttpServletRequest
import java.text.NumberFormat

@Component
@CompileStatic
class CsvReader extends RecordsLoader {

    @Autowired
    GrailsApplication grailsApplication

    @CompileStatic(TypeCheckingMode.SKIP)
    List<Map> load(String file) {

        List<Map> results = []
        Resource resource = grailsApplication.mainContext.getResource("classpath:${file}.csv")
        assert resource.exists(), "File $file does not exist"

        CSVMapReader reader = new CSVMapReader(new InputStreamReader(resource.inputStream))
        reader.each { Map m ->
            //need to convert to grails parameter map, so that it can be binded
            cleanup(m)
            //m = toGrailsParamsMap(m)
            results.add(m)
        }

        return results

    }

    void cleanup(Map m) {
        ['latitude', "longitude"].each { key ->
            if (m[key]) m[key] = NumberFormat.getInstance().parse(m[key] as String)
        }
        if (m.containsKey("country.id")) {
            m["country"] = [:]
            m["country"]["id"] = m["country.id"]
            m.remove("country.id")
        }
        if (m.containsKey("region.id")) {
            m["region"] = [:]
            m["region"]["id"] = m["region.id"]
            m.remove("region.id")
        }
    }

    protected void updateNestedKeys(Map initialMap) {
        Map wrappedMap = [:]
        for (String key : initialMap.keySet()) {
            final int nestedIndex = key.indexOf('.')
            if (nestedIndex == -1) {
                wrappedMap.put(key, initialMap[key])
            } else {
                processNestedKeys(initialMap, key, key, wrappedMap)
            }
        }
    }

    /*
 * Builds up a multi dimensional hash structure from the parameters so that nested keys such as
 * "book.author.name" can be addressed like params['author'].name
 *
 * This also allows data binding to occur for only a subset of the properties in the parameter map.
 */

    private void processNestedKeys(Map requestMap, String key, String nestedKey, Map nestedLevel) {
        final int nestedIndex = nestedKey.indexOf('.');
        if (nestedIndex == -1) {
            return;
        }

        // We have at least one sub-key, so extract the first element
        // of the nested key as the prfix. In other words, if we have
        // 'nestedKey' == "a.b.c", the prefix is "a".
        String nestedPrefix = nestedKey.substring(0, nestedIndex);

        // Let's see if we already have a value in the current map for the prefix.
        Object prefixValue = nestedLevel.get(nestedPrefix);
        if (prefixValue == null) {
            // No value. So, since there is at least one sub-key,
            // we create a sub-map for this prefix.
            prefixValue = [:]
            nestedLevel[nestedPrefix] = prefixValue
        }

        // If the value against the prefix is a map, then we store the sub-keys in that map.
        if (!(prefixValue instanceof Map)) {
            //should blow an error as we may have something like ['a.b':"foo", a:"foo"] which should blow it up
            return;
        }

        Map nestedMap = (Map) prefixValue;
        if (nestedIndex < nestedKey.length() - 1) {
            String remainderOfKey = nestedKey.substring(nestedIndex + 1, nestedKey.length());
            nestedMap[remainderOfKey] = requestMap[key]
            if (remainderOfKey.indexOf('.') > -1) {
                processNestedKeys(requestMap, remainderOfKey, remainderOfKey, nestedMap);
            }
        }

    }
//s
}
