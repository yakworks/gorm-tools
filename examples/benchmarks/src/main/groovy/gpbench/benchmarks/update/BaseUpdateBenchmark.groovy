package gpbench.benchmarks.update

import java.util.concurrent.ThreadLocalRandom

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.databinding.EntityMapBinder
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoLookup
import gpbench.benchmarks.BaseBatchInsertBenchmark
import gpbench.model.Region

/**
 * Baseline benchmark with grails out of the box
 */

@CompileStatic
abstract class BaseUpdateBenchmark<T> extends BaseBatchInsertBenchmark<T> {

    EntityMapBinder entityMapBinder
    GormRepo<T> repo
    List<Map> citiesUpdated

    BaseUpdateBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod, validate)
        repo = RepoLookup.findRepo(clazz)
    }

    //region ids are not sequential, so we need to keep reference of list of region ids to select one of it randomly)
    List regionIds
    Long regionCount

    @CompileDynamic
    void setup() {
        super.setup()
        regionIds = Region.executeQuery("select id from Region")
        regionCount = Region.count()

        parallelTools.parallel(cities) { List<Map> list ->
            repo.batchTrx(list) { Map item ->
                repo.doCreate(item, [:])
            }
            updateRows(list)
        }
        //make sure cities are inserted
        assert  domainClass.count() != 0
        citiesUpdated = cities.flatten()
    }

    @CompileDynamic
    void updateRows(List<Map> list) {
        for(Map row : list) {
            BigDecimal latitude = (row.latitude as BigDecimal)
            BigDecimal longitude = (row.longitude as BigDecimal)


            if(latitude < 89.00) latitude = latitude + 1
            if(longitude < 378.00) longitude = longitude + 1

            row.latitude = latitude.toString()
            row.longitude = longitude.toString()
            row.name = row.name + "-Updated"
            row.shortCode = row.shortCode + "-Updated"
            row.region = [id:randomRegionId]
            row.country = [id:randomCountryId]
        }
    }

    /*
    TODO remove
    @CompileDynamic
    Map getUpdateData(GormEntity<T> entity) {
        Map data = [:]

        List<PersistentProperty> props = domainClass.gormPersistentEntity.persistentProperties
        for(PersistentProperty p : props) {
            data[p.name] = getUpdatedValue(entity, p)
        }

        return data
    }

    @CompileDynamic
    def getUpdatedValue(GormEntity entity, PersistentProperty p) {
        if(p instanceof Association)  return [id: getUpdatedAssociation(entity, (Association)p)]
        def originalValue = entity.getPersistentValue(p.name)
        if(originalValue instanceof String) return originalValue + "-Updated"
        if(originalValue instanceof Number) return  originalValue + 1
        if(originalValue instanceof Data) return ((Date)originalValue).plus(1)
        else return originalValue
    }

    def getUpdatedAssociation(GormEntity entity, Association association) {
        if(association.type.isAssignableFrom(Country)) return getRandomCountryId()
        if(association.type.isAssignableFrom(Region)) return getRandomRegionId()
    }*/

    Long getRandomCountryId() {
        return ThreadLocalRandom.current().nextLong(1, 276)
    }

    Long getRandomRegionId() {
        int rand = ThreadLocalRandom.current().nextInt(1, (int)regionCount)
        return regionIds.get(rand)
    }

}
