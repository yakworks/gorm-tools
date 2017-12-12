package gpbench.benchmarks

/**
 * Created by sudhir on 17/11/17.
 */
class DatabindingBenchmark extends AbstractBenchmark {

    Class domain

    Map props = ['name': 'test', 'shortCode':'test', 'latitude':"10.10", 'longitude': "10.10", 'region': null, 'country': null]

    DatabindingBenchmark(Class domain) {
        this.domain = domain
    }

    @Override
    protected execute() {
        for (int i in (1..1_000_00_0)) {
            def instance = domain.newInstance()
            instance.properties = props
        }
    }

    String getDescription() {
       return "DatabindingBenchmark<$domain.simpleName> 1 Million records"
    }
}
