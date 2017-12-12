package gpbench

import gorm.tools.GormUtils
import gorm.tools.beans.DateUtil
import gorm.tools.databinding.FastBinder
import gpbench.fat.CityFatNoTraits
import gpbench.fat.CityFatSimple
import gpbench.fat.CityFat
import gpbench.fat.CityFatDynamic
import gpbench.helpers.JsonReader
import gpbench.helpers.RecordsLoader
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

class BenchmarkDatabindingService {
    JsonReader jsonReader
    FastBinder fastBinder

    Long count = 111690
    Map props = [
        'name': 'test', 'shortCode':'test', state:"Gujarat", "countryName": "india", 'latitude':"10.10", 'longitude': "10.10"]
//    ,
//        'name2': 'test', 'shortCode2':'test', state2:"Gujarat", "country2": "india", 'latitude2':"10.10", 'longitude2': "10.10",
//        'name3': 'test', 'shortCode4':'test', state3:"Gujarat", "country3": "india", 'latitude3':"10.10", 'longitude3': "10.10",]

    boolean mute = false
    int warmupCityTimes = 3
    int loadCityTimes = 3
    List cities

    def runFat(){
        println "run load json file 3x number of fields"

        jsonReader._cache = [:]
        loadCities3xProps(loadCityTimes)
        println "Warm up logan  "
        mute = true
        (1..2).each {
            if(!mute) println "\n - setters, copy and fast bind on simple no associations"
            useSetPropsFastIterate(CityFatSimple)
            if(!mute) println "\n - setters or property copy on associations with 20 fields"
            useStaticSettersInDomain(CityFat)
            useSetPropsFastIterate(CityFat)
            useFastBinder(CityFat)
            if(!mute) println " - Dynamic setters are slower"
            useDynamicSettersFat(CityFat)
            if(!mute) println " - Slower without @GrailsCompileStatic on domain"
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
        eachCity("useDatabinding", domain){ instance , Map row ->
            instance.properties = row
        }
    }

    void useStaticSettersInDomain(Class domain) {
        eachCity("useStaticSettersInDomain", domain){ instance , Map row ->
            instance.setPropsFast(row)
        }
    }

    void useSetPropsFastIterate(Class domain) {
        eachCity("setPropsFastIterate", domain){ instance , Map row ->
            setPropsFastIterate(instance, row)
        }
    }

    void gormUtilsBindFast(Class domain) {
        eachCity("gormUtilsBindFast", domain){ instance , Map row ->
            GormUtils.bindFast(instance, row)
            //setPropsFastIterate(instance, row)
        }
    }

    void useFastBinder(Class domain) {
        eachCity("useFastBinder", domain){ instance , Map row ->
            fastBinder.bind(instance, row)
        }
    }

    void useSettersDynamicSimple(Class domain) {
        eachCity("useDynamicSettersFat", domain){ instance , Map row ->
            instance.name = row['name']
            instance.shortCode = row['shortCode']
            instance.state = row['state']
            instance.countryName = row['countryName']
            instance.latitude = row['latitude'] as BigDecimal
            instance.longitude = row['longitude'] as BigDecimal
        }
    }

    void useDynamicSettersFat(Class domain) {

        eachCity("useDynamicSettersFat", domain){ instance , Map row ->
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
            instance.date1 = DateUtil.parseJsonDate(row['date1'] as String)
            instance.date2 = DateUtil.parseJsonDate(row['date2'] as String)
            instance.date3 = DateUtil.parseJsonDate(row['date3'] as String)
            instance.date4 = DateUtil.parseJsonDate(row['date4'] as String)

            setAssociations(instance, "region", Region, row)
            setAssociations(instance, "country", Country, row)
            setAssociations(instance, "region2", Region, row)
            setAssociations(instance, "country2", Country, row)
            setAssociations(instance, "region3", Region, row)
            setAssociations(instance, "country3", Country, row)
        }
    }

    void setAssociations(instance, String key, Class assocClass, Map row){
        if(instance.hasProperty(key) && row[key] && row[key].id){
            instance[key] = assocClass.load(row[key].id)
        }
    }

    void eachCity(String msg, Class domain, Closure rowClosure) {
        StopWatch watch = new StopWatch()
        watch.start()
        for (Map row in cities) {
            GormEntity instance = domain.newInstance()
            rowClosure.call(instance, row)
            //instance.validate([failOnError:true])
        }
        watch.stop()
        if(!mute) println "${watch.totalTimeSeconds}s $msg $domain.simpleName | ${cities.size()} rows"
    }

    void loadCities(int mult) {
        RecordsLoader recordsLoader = jsonReader //useDatabinding ? csvReader : jsonReader
        List cityfull = recordsLoader.read("City")
        for (Map row in cityfull) {
            row.state  = row.region.id
            row.countryName  = row.country.id
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
            row.region2 = [id:row.region.id]
            row.region3 = [id:row.region.id]

            row.country2 = [id:row.country.id]
            row.country3 = [id:row.country.id]

            row.state  = row.region.id
            row.countryName  = row.country.id
            row.state2  = row.region.id
            row.countryName2  = row.country.id
            row.state3  = row.region.id
            row.countryName3  = row.country.id

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

    DateFormat dateFormat = new SimpleDateFormat('yyyy-MM-dd')
    @CompileStatic
    Object setPropsFastIterate(Object obj, Map source, boolean ignoreAssociations = false) {
        //if (target == null) throw new IllegalArgumentException("Target is null")
        if (source == null) return

        def sapi = GormEnhancer.findStaticApi(obj.getClass())
        def properties = sapi.gormPersistentEntity.getPersistentProperties()
        for (PersistentProperty prop : properties){
            if(!source.containsKey(prop.name)) {
                continue
            }
            def sval = source[prop.name]
            def valToAssisgn = sval
            Class typeToConvertTo = prop.getType()
            if (prop instanceof Association && sval['id']) {
                if(ignoreAssociations) return
                def asocProp = (Association)prop
                def asc = GormEnhancer.findStaticApi(asocProp.associatedEntity.javaClass).load(sval['id'] as Long)
                valToAssisgn = asc
            }
            else if (sval instanceof String) {
                if(Number.isAssignableFrom(typeToConvertTo)){
                    valToAssisgn = (sval as String).asType(typeToConvertTo)
                }
                else if(Date.isAssignableFrom(typeToConvertTo)){
                    //valToAssisgn = dateFormat.parse(sval as String)
                    valToAssisgn = DateUtil.parseJsonDate(sval as String)
                    //println "converted $sval to ${valToAssisgn} for $prop.name with DateUtil.parseJsonDate"
                }
                else if(conversionHelpers.containsKey(typeToConvertTo)){
                    def convertersList = conversionHelpers.get(typeToConvertTo)
                    ValueConverter converter = convertersList?.find { ValueConverter c -> c.canConvert(sval) }
                    if (converter) {
                        valToAssisgn= converter.convert(sval)
                        //println new Date()
                        //println "converted $sval to ${valToAssisgn} for $prop.name with ${converter.class.name}"
                    }
                }
            }

            // all else fails let groovy bind it
            obj[prop.name] = valToAssisgn

            //println prop
            //println "${prop.name}: ${obj[prop.name]} -> region:${obj.region}"
        }
        return obj
    }


    @Autowired(required=true)
    @CompileStatic
    void setValueConverters(ValueConverter[] converters) {
        converters.each { ValueConverter converter ->
            registerConverter converter
        }
    }
    protected Map<Class, List<ValueConverter>> conversionHelpers = [:].withDefault { c -> [] }

    @CompileStatic
    void registerConverter(ValueConverter converter) {
        //println converter.targetType
        //conversionHelpers[converter.targetType] == converter
        conversionHelpers[converter.targetType] << converter
    }

//    @Autowired(required=false)
//    void setFormattedValueConverters(FormattedValueConverter[] converters) {
//        converters.each { FormattedValueConverter converter ->
//            registerFormattedValueConverter converter
//        }
//    }


}
