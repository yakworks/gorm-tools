package gpbench.benchmarks.update

import gorm.tools.databinding.EntityMapBinder
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.api.RepositoryApi
import gpbench.Country
import gpbench.Region
import gpbench.benchmarks.BaseBatchInsertBenchmark
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association

import javax.xml.crypto.Data
import java.util.concurrent.ThreadLocalRandom

/**
 * Baseline benchmark with grails out of the box
 */

@CompileStatic
abstract class BaseUpdateBenchmark<T extends GormEntity & WebDataBinding> extends BaseBatchInsertBenchmark<T> {

    EntityMapBinder entityMapBinder
    RepositoryApi<T> repo

    BaseUpdateBenchmark(Class<T> clazz, String bindingMethod = 'grails', boolean validate = true) {
        super(clazz, bindingMethod, validate)
        repo = RepoUtil.findRepository(clazz)
    }

    //region ids are not sequential, so we need to keep reference of list of region ids to select one of it randomly)
    List regionIds
    Long regionCount

    @CompileDynamic
    void setup() {
        super.setup()
        asyncBatchSupport.parallel(cities) { List<Map> list, Map args ->
            repo.batchCreate(list)
        }
        //make sure cities are inserted
        assert  domainClass.count() != 0
        regionIds = Region.executeQuery("select id from Region")
        regionCount = Region.count()
    }

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
    }

    Long getRandomCountryId() {
        return ThreadLocalRandom.current().nextLong(1, 276)
    }

    Long getRandomRegionId() {
        int rand = ThreadLocalRandom.current().nextInt(1, (int)regionCount)
        return regionIds.get(rand)
    }

}
