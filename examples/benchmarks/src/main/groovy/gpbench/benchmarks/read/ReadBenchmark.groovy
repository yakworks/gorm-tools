package gpbench.benchmarks.read

import gpbench.basic.CityBasicRepo
import gpbench.benchmarks.legacy.BaseBenchmark
import groovyx.gpars.GParsPool
import gpbench.basic.CityBasic
import org.springframework.transaction.annotation.Transactional

/**
 * Runs batch 'reads' in parallel.
 */
@Transactional
class ReadBenchmark extends BaseBenchmark {

    CityBasicRepo cityRepo
    int poolSize

    /**
     * If true, data is read in multiple threads. In this case the test works
     * as if there are several users trying to read data at the same time.
     */
    boolean useMultiUserEnvironment

    /**
     * List with ids of saved records.
     */
    List<Long> ids

    ReadBenchmark(boolean databinding, boolean useMultiUserEnvironment = true) {
        super(databinding)
        this.useMultiUserEnvironment = useMultiUserEnvironment
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
        assert CityBasic.count() == 37230
    }

    @Override
    def execute() {
        //read the first time to copy to second level cache
        read()
        if (useMultiUserEnvironment) {
            GParsPool.withPool(poolSize) {
                (1..poolSize).eachParallel {
                    CityBasic.withNewSession { read() }
                }
            }
        } else {
            read()
        }
    }

    void read() {
        CityBasic currentRecord

        ids.each { Long id ->
            currentRecord = cityRepo.get(id, null)
        }
    }

    @Override
    String getDescription() {
        return "ReadBenchmark: multiple users=${useMultiUserEnvironment}"
    }

}
