package gpbench

import gorm.tools.beans.IsoDateUtil
import gpbench.fat.CityFat
import gpbench.fat.CityFatDynamic
import gpbench.fat.CityFatNoTraitsDynamic
import gpbench.fat.CityFatNoTraits
import gpbench.fat.CityFatNoTraitsNoAssoc
import gpbench.traits.BenchDataInsert
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * single threaded sanity checks
 */
@CompileStatic
class CityFatBenchInsertService extends BenchDataInsert {

    void runBenchMarks() {
        setup()

        ["setters/databinding", "validate", "save batch", "save async"].each{
            createAction = it
            println "-- createAction: $createAction --"
            settersStaticNoAssoc()
            settersStatic(CityFat)
            settersDynamic(CityFat)
            gormToolsFast(CityFat)
        }
        saveStatsToJsonFile "CityFatBench.json"

        logMessage "\n**** The slower ones ****"
        ["setters/databinding", "validate", "save batch", "save async"].each{
            createAction = it
            println "-- createAction: $createAction --"
            //settersStaticNoAssoc() //this is fast and kept here for reference
            settersStatic(CityFatDynamic)
            settersDynamic(CityFatDynamic)
            gormToolsFast(CityFatDynamic)
        }
        saveStatsToJsonFile "CityFatBench.json"

        createAction = "setters/databinding"
        binderType = 'grails'
        benchKey = 'stockGrailsBinderNoTraits'

        logMessage "\n-- Grails default DataBinder --"
        insertData(CityFatNoTraits, dataList)
        insertData(CityFatNoTraitsDynamic, dataList)
        //warmUpAndInsert(CityFatNoTraits)

        logMessage "*** Using traits with the Grails default DataBinder is super slow, see bug report"
        benchKey = 'stockGrailsBinderWithTraits'
        binderType = 'grails'
        insertData(CityFat, dataList)
        //warmUpAndInsert(CityFat)

        saveStatsToJsonFile "CityFatBench.json"

    }

    //@Transactional
    void settersStaticNoAssoc(){
        binderType = 'settersStatic'
        benchKey = 'setters static, no associations'
        //no warm up on this one
        insertData(CityFatNoTraitsNoAssoc, dataList)
    }

    void settersStatic(Class domainClass){
        binderType = 'settersStatic'
        benchKey = 'setters static'
        insertData(domainClass, dataList)
        //insertData(CityFatDynamic, dataList)
//        warmUpAndInsert(CityFat)
//        warmUpAndInsert(CityFatDynamic)
    }

    void settersDynamic(Class domainClass){
        binderType = 'settersDynamic'
        benchKey = 'setters dynamic'
        insertData(domainClass, dataList)
        //insertData(CityFatDynamic, dataList)
//        warmUpAndInsert(CityFat)
//        warmUpAndInsert(CityFatDynamic)
    }

    void gormToolsFast(Class domainClass){
        binderType = 'fast'
        benchKey = 'gorm-tools: repository & fast binder'
        insertData(domainClass, dataList)
        //insertData(CityFatDynamic, dataList)
        //warmUpAndInsert(CityFat)
        //warmUpAndInsert(CityFatDynamic)
    }


    @Override
    void loadData(){
        println "run load city data json file 3x number of fields"
        //jsonReader._cache = [:]
        dataList = jsonReader.loadCityFatData(loadIterations)

    }

    @Override
    void loadWarmUpData(){
        println "loadWarmUpData"
        //jsonReader._cache = [:]
        warmupDataList = jsonReader.loadCityFatData(1)

    }

    @CompileDynamic
    @Override
    void setterDynamic(instance, row) {
        instance.with {
            name = row['name']
            shortCode = row['shortCode']
            state = row['state']
            countryName = row['countryName']
            latitude = row['latitude'] as BigDecimal
            longitude = row['longitude'] as BigDecimal

            name2 = row['name2']
            shortCode2 = row['shortCode2']
            state2 = row['state2']
            countryName2 = row['countryName2']
            latitude2 = row['latitude2'] as BigDecimal
            longitude2 = row['longitude2'] as BigDecimal

            name3 = row['name3']
            shortCode3 = row['shortCode3']
            state3 = row['state3']
            countryName3 = row['countryName3']
            latitude3 = row['latitude3'] as BigDecimal
            longitude3 = row['longitude3'] as BigDecimal
            //this.properties = row
            date1 = IsoDateUtil.parse(row['date1'] as String)
            date2 = IsoDateUtil.parseLocalDate(row['date2'] as String) //DateUtil.parse(row['date2'] as String)
            date3 = IsoDateUtil.parseLocalDateTime(row['date3'] as String)
            date4 = IsoDateUtil.parseLocalDate(row['date4'] as String)
        }
        setAssociation(instance, "region", Region, row)
        setAssociation(instance, "region2", Region, row)
        setAssociation(instance, "region3", Region, row)
        setAssociation(instance, "country", Country, row)
        setAssociation(instance, "country2", Country, row)
        setAssociation(instance, "country3", Country, row)
    }

    @CompileDynamic
    void setAssociation(instance, String key, Class assocClass, Map row) {
        if (row[key]) {
            Long id = row[key]['id'] as Long
            instance[key] = assocClass.load(id)
        }
    }

}
