package gpbench.benchmarks

import gpbench.helpers.RecordsLoader
import org.grails.datastore.gorm.GormEntity

/**
 * Baseline benchmark with grails out of the box
 */

//@GrailsCompileStatic
class GparsFatBenchmark<T extends GormEntity> extends GparsBaselineBenchmark<T> {

    GparsFatBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod,validate)
    }

    void setup() {
        super.setup()
        RecordsLoader recordsLoader = jsonReader //useDatabinding ? csvReader : jsonReader
        List cityfull = recordsLoader.read("City")

        for (Map row in cityfull) {
            row.region2 = [id:row.region.id]
            row.region3 = [id:row.region.id]

            row.country2 = [id:row.country.id]
            row.country3 = [id:row.country.id]

            //row.state  = row.region.id
            //row.countryName  = row.country.id
            row.state2  = row.region.id
            row.countryName2  = row.country.id
            row.state3  = row.region.id
            row.countryName3  = row.country.id

            row.name2 = row.name
            row.shortCode2 = row.shortCode
            row.latitude2 = row.latitude
            row.longitude2 = row.longitude

            row.name3 = row.name
            row.shortCode3 = row.shortCode
            row.latitude3 = row.latitude
            row.longitude3 = row.longitude
            //row.remove('region')
            //row.remove('country')
            //instance.properties = row
        }

        List repeatedCity = []
        (1..repeatedCityTimes).each { i ->
            repeatedCity = repeatedCity + cityfull
        }
        cities = repeatedCity.collate(batchSize)
    }
}
