package gpbench.benchmarks

import gpbench.CityRepo
import groovyx.gpars.GParsPool
import gpbench.City
import org.springframework.transaction.annotation.Transactional

/**
 * Runs batch 'reads' in parallel.
 */
@Transactional
class ReadBenchmark extends BaseBenchmark {

    CityRepo cityRepo
    int poolSize
    boolean showStatistics

    /**
     * If true, data is read in multiple threads. In this case the test works
     * as if there are several users trying to read data at the same time.
     */
    boolean useMultiUserEnvironment

    /**
     * List with ids of saved records.
     */
    List<Long> ids

    ReadBenchmark(boolean databinding, boolean useMultiUserEnvironment = true, boolean showStatistics = true) {
        super(databinding)
        this.useMultiUserEnvironment = useMultiUserEnvironment
        this.ids = Collections.synchronizedList([])
        this.showStatistics = showStatistics
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
        //read the first time to copy to second level cache
        read()
        if (useMultiUserEnvironment) {
            GParsPool.withPool(poolSize) {
                (1..poolSize).eachParallel {
                    City.withNewSession { read() }
                }
            }
        } else {
            read()
        }
    }

    void read() {
        City currentRecord

        ids.each { Long id ->
            currentRecord = cityRepo.get(id, null)
        }
    }

    @Override
    String getDescription() {
        return "ReadBenchmark: multiple users=${useMultiUserEnvironment}"
    }

}
