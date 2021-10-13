package gpbench.helpers


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

import gorm.tools.json.JsonParserTrait
import grails.core.GrailsApplication

@Component
@CompileStatic
class JsonReader extends RecordsLoader implements JsonParserTrait {

    @Autowired
    GrailsApplication grailsApplication

    List<Map> load(String file) {

        List<Map> results = []
        Resource resource = grailsApplication.mainContext.getResource("classpath:${file}.json")
        assert resource.exists(), "File $file does not exist"

        String line
        resource.inputStream.withReader { Reader reader ->
            while (line = reader.readLine()) {
                Map json = parseJsonText(line) as Map
                results.add json
            }
        }

        return results
    }

    @CompileDynamic
    List<Map> loadCityFatData(int mult) {
        //RecordsLoader recordsLoader = jsonReader //useDatabinding ? csvReader : jsonReader
        List cityfull = read("City")
        for (Map row in cityfull) {
            row.region2 = [id: row.region.id]
            row.region3 = [id: row.region.id]

            row.country2 = [id: row.country.id]
            row.country3 = [id: row.country.id]

            row.state = row.region.id
            row.countryName = row.country.id
            row.state2 = row.region.id
            row.countryName2 = row.country.id
            row.state3 = row.region.id
            row.countryName3 = row.country.id

            row.name2 = row.name
            row.shortCode2 = row.shortCode.toString()
            row.latitude2 = row.latitude.toString()
            row.longitude2 = row.longitude.toString()

            row.name3 = row.name
            row.shortCode3 = row.shortCode
            row.latitude3 = row.latitude.toString()
            row.longitude3 = row.longitude.toString()

            row.date1 = '2017-11-20T23:28:56.782Z'
            row.date2 = '2017-11-22'
            row.date3 = '2017-11-22T23:28:56.782Z'
            row.date4 = '2017-11-23'
            //row.remove('region')
            //row.remove('country')
            //instance.properties = row
            //row.localDate =
        }
        List repeatedCity = []
        (1..mult).each { i ->
            repeatedCity = repeatedCity + cityfull
        }
        println "JSON read and built for CityFat for ${repeatedCity.size()} records"
        return repeatedCity
        //cities = repeatedCity.collate(batchSize)
    }

}
