package gpbench

import gorm.tools.beans.IsoDateUtil
import gorm.tools.repository.RepoUtil
import gpbench.fat.CityFat
import gpbench.fat.CityFatDynamic
import gpbench.fat.CityFatDynamicNoTraits
import gpbench.fat.CityFatNoTraits
import gpbench.fat.CityFatNoTraitsNoAssoc
import gpbench.traits.BenchConfig
import gpbench.traits.BenchDataInsert
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity

/**
 * single threaded sanity checks
 */
@CompileStatic
class CityFatBenchService extends BenchDataInsert {

    void runBenchMarks() {
        setup()

//        ["bind/setters only", "validate"].each{
//            createAction = it
//            println "** createAction: $createAction"
//            settersStaticNoAssoc()
//            settersStatic()
//            settersDynamic()
//            gormToolsFast()
//
//        }

        ["save", "save batch", "save async"].each{
            createAction = it
            println "-- createAction: $createAction --"
            settersDynamic()
            settersStatic()
            gormToolsFast()
            settersStaticNoAssoc()
        }


        createAction = "bind/setters only"
        binderType = 'grails'
        msgKey = 'stockGrailsBinderNoTraits'
        warmUpAndInsert(CityFatNoTraits)
        warmUpAndInsert(CityFatDynamicNoTraits)

        logMessage "* Using traits with grails databinding is super slow, see bug report"
        msgKey = 'stockGrailsBinderWithTraits'
        binderType = 'grails'
        warmUpAndInsert(CityFat)
        //warmUpAndInsert(CityFatDynamic)

    }

    //@Transactional
    void settersStaticNoAssoc(){
        binderType = 'settersStatic'
        msgKey = 'setters static, no associations'
        //no warm up on this one
        insertData(CityFatNoTraitsNoAssoc, dataList)
    }

    void settersStatic(){
        binderType = 'settersStatic'
        msgKey = 'setters static'
        insertData(CityFat, dataList)
        insertData(CityFatDynamic, dataList)
//        warmUpAndInsert(CityFat)
//        warmUpAndInsert(CityFatDynamic)
    }

    void settersDynamic(){
        binderType = 'settersDynamic'
        msgKey = 'setters dynamic'
        insertData(CityFat, dataList)
        insertData(CityFatDynamic, dataList)
//        warmUpAndInsert(CityFat)
//        warmUpAndInsert(CityFatDynamic)
    }

    void gormToolsFast(){
        binderType = 'fast'
        msgKey = 'gorm-tools: repository & fast binder'
        insertData(CityFat, dataList)
        insertData(CityFatDynamic, dataList)
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
