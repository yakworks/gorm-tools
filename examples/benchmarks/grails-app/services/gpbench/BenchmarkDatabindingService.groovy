package gpbench

import gorm.tools.GormUtils
import gorm.tools.beans.IsoDateUtil
import gorm.tools.databinding.EntityMapBinder
import gpbench.fat.CityFat
import gpbench.fat.CityFatDynamic
import gpbench.fat.CityFatNoTraits
import gpbench.fat.CityFatSimple
import gpbench.helpers.JsonReader
import gpbench.helpers.RecordsLoader
import grails.databinding.SimpleMapDataBindingSource
import grails.databinding.converters.ValueConverter
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.StopWatch

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BenchmarkDatabindingService {
    JsonReader jsonReader

    @Autowired
    EntityMapBinder entityMapBinder

    Long count = 111690
    Map props = [
        'name': 'test', 'shortCode': 'test', state: "Gujarat", "countryName": "india", 'latitude': "10.10", 'longitude': "10.10"]
//    ,
//        'name2': 'test', 'shortCode2':'test', state2:"Gujarat", "country2": "india", 'latitude2':"10.10", 'longitude2': "10.10",
//        'name3': 'test', 'shortCode4':'test', state3:"Gujarat", "country3": "india", 'latitude3':"10.10", 'longitude3': "10.10",]

    boolean mute = false
    int warmupCityTimes = 3
    int loadCityTimes = 3
    List cities

    def runFat() {
        println "run load json file 3x number of fields"

        jsonReader._cache = [:]
        loadCities3xProps(loadCityTimes)
        println "Warm up pass "
        mute = true
        (1..2).each {
            if (!mute) println "\n - fast bind on simple, no dates or associations"
            useFastBinder(CityFatSimple)
            if (!mute) println "\n - fat with 20+ fields, has dates and associations"
            useStaticSettersInCityFat()
            useFastBinder(CityFat)
            useEntityBinderBind(CityFat)
            mute = false
        }

        println "Warm up pass "
        mute = true
        (1..2).each {
            if (!mute) println " - Dynamic setters are slower"
            useDynamicSettersFat(CityFat)
            if (!mute) println " - Slower without @GrailsCompileStatic on domain"
            useDynamicSettersFat(CityFatDynamic)
            useFastBinder(CityFatDynamic)
            mute = false
        }

        println "\n - Binding is slower"
        useDatabinding(CityFatNoTraits)
        println "\n - Binding with Traits are very slow, especially with associations"
        useDatabinding(CityFat)
        //println " - And SUPER SLOW when not using @CompileStatic"
        //useDatabinding(CityFatDynamic)

    }

    void useDatabinding(Class domain) {
        eachCity("useDatabinding", domain) { instance, Map row ->
            instance.properties = row
        }
    }

    void useStaticSettersInDomain(Class domain) {
        eachCity("useStaticSettersInDomain", domain) { instance, Map row ->
            instance.setPropsFast(row)
        }
    }

    @CompileStatic
    void useStaticSettersInCityFat() {
        eachCity("useStaticSettersInDomain", CityFat) { CityFat instance, Map row ->
            instance.setPropsFast(row)
        }
    }

    void useSetPropsFastIterate(Class domain) {
        eachCity("setPropsFastIterate", domain) { instance, Map row ->
            setPropsFastIterate(instance, row)
        }
    }
//
//    void gormUtilsBindFast(Class domain) {
//        eachCity("gormUtilsBindFast", domain) { instance, Map row ->
//            GormUtils.bindFast(instance, row)
//            //setPropsFastIterate(instance, row)
//        }
//    }

    @CompileStatic
    void useFastBinder(Class domain) {
        eachCity("useEntityBinder.fastBind", domain) { instance, Map row ->
            entityMapBinder.fastBind(instance, new SimpleMapDataBindingSource(row))
        }
    }

    @CompileStatic
    void useEntityBinderBind(Class domain) {
        eachCity("useEntityBinderBind", domain) { instance, Map row ->
            entityMapBinder.bind(instance, row)
        }
    }

    void useSettersDynamicSimple(Class domain) {
        eachCity("useDynamicSettersFat", domain) { instance, Map row ->
            instance.name = row['name']
            instance.shortCode = row['shortCode']
            instance.state = row['state']
            instance.countryName = row['countryName']
            instance.latitude = row['latitude'] as BigDecimal
            instance.longitude = row['longitude'] as BigDecimal
        }
    }

    void useDynamicSettersFat(Class domain) {

        eachCity("useDynamicSettersFat", domain) { instance, Map row ->
            instance.name = row['name']
            instance.shortCode = row['shortCode']
            instance.state = row['state']
            instance.countryName = row['countryName']
            instance.latitude = row['latitude'] as BigDecimal
            instance.longitude = row['longitude'] as BigDecimal

            instance.name2 = row['name2']
            instance.shortCode2 = row['shortCode2']
            instance.state2 = row['state2']
            instance.countryName2 = row['countryName2']
            instance.latitude2 = row['latitude2'] as BigDecimal
            instance.longitude2 = row['longitude2'] as BigDecimal

            instance.name3 = row['name3']
            instance.shortCode3 = row['shortCode3']
            instance.state3 = row['state3']
            instance.countryName3 = row['countryName3']
            instance.latitude3 = row['latitude3'] as BigDecimal
            instance.longitude3 = row['longitude3'] as BigDecimal
            //instance.properties = row
            instance.date1 = IsoDateUtil.parse(row['date1'] as String)
            instance.date2 = LocalDate.parse(row['date2'] as String)
            instance.date3 = LocalDateTime.parse(row['date3'] as String, DateTimeFormatter.ISO_DATE_TIME)
            instance.date4 = LocalDate.parse(row['date4'] as String)

            setAssociations(instance, "region", Region, row)
            setAssociations(instance, "country", Country, row)
            setAssociations(instance, "region2", Region, row)
            setAssociations(instance, "country2", Country, row)
            setAssociations(instance, "region3", Region, row)
            setAssociations(instance, "country3", Country, row)
        }
    }

    void setAssociations(instance, String key, Class assocClass, Map row) {
        if (instance.hasProperty(key) && row[key] && row[key].id) {
            instance[key] = assocClass.load(row[key].id)
        }
    }

    @CompileStatic
    void eachCity(String msg, Class domain, Closure rowClosure) {
        StopWatch watch = new StopWatch()
        watch.start()
        for (Object row in cities) {
            GormEntity instance = domain.newInstance()
            rowClosure.call(instance, (Map) row)
            //instance.validate([failOnError:true])
        }
        watch.stop()
        if (!mute) println "${watch.totalTimeSeconds}s $msg $domain.simpleName | ${cities.size()} rows"
    }

    void loadCities(int mult) {
        RecordsLoader recordsLoader = jsonReader //useDatabinding ? csvReader : jsonReader
        List cityfull = recordsLoader.read("City")
        for (Map row in cityfull) {
            row.state = row.region.id
            row.countryName = row.country.id
            row.remove('region')
            row.remove('country')
            //instance.properties = row
        }
        List repeatedCity = []
        (1..mult).each { i ->
            repeatedCity = repeatedCity + cityfull
        }
        cities = repeatedCity
        //cities = repeatedCity.collate(batchSize)
    }

    void loadCities3xProps(int mult) {
        RecordsLoader recordsLoader = jsonReader //useDatabinding ? csvReader : jsonReader
        List cityfull = recordsLoader.read("City")
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
        cities = repeatedCity
        //cities = repeatedCity.collate(batchSize)
    }

}
