package gpbench.helpers

import grails.converters.JSON
import grails.core.GrailsApplication
import groovy.transform.CompileStatic
import org.grails.web.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

@Component
@CompileStatic
class JsonReader extends RecordsLoader {

    @Autowired
    GrailsApplication grailsApplication

    List<Map> load(String file) {

        List<Map> results = []
        Resource resource = grailsApplication.mainContext.getResource("classpath:${file}.json")
        assert resource.exists(), "File $file does not exist"

        String line
        resource.inputStream.withReader { Reader reader ->
            while (line = reader.readLine()) {
                JSONObject json = (JSONObject) JSON.parse(line)
                results.add json
            }
        }

        return results
    }

}
