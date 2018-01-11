package gpbench.benchmarks

import gpbench.City
import gpbench.CityRepo
import groovyx.gpars.GParsPool
import org.springframework.transaction.annotation.Transactional

@Transactional
class UpdateBenchmark extends BaseBenchmark {

    CityRepo cityRepo
    int poolSize

    /**
     * List with ids of saved records.
     */
    List<Long> ids

    UpdateBenchmark(boolean databinding) {
        super(databinding)
        this.ids = Collections.synchronizedList([])
    }

    @Override
    void setup() {
        super.setup()

        GParsPool.withPool(poolSize) {
            cities.eachParallel { Map record ->
                try {
                    ids.add(cityRepo.create(record).id)
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
        assert City.count() == 37230
    }

    @Override
    def execute() {
        //reading records for the first time to copy them to second level cache
        ids.each { Long id -> cityRepo.get(id, null) }

        GParsPool.withPool(poolSize) {
            ids.eachParallel { Long id -> update(id) }
        }
    }

    void update(Long id) {
        City.withNewSession {
            cityRepo.update([flush: true], [id: id, name: "cityId=${id}"])
        }
    }

    @Override
    String getDescription() {
        return "UpdateBenchmark"
    }

}
