package yakworks.testify

import yakworks.rally.seed.RallySeed
import yakworks.testing.gorm.model.KitchenSink

class BootStrap {

    def init = { servletContext ->
        RallySeed.fullMonty()
        buildKitchen()
    }

    def destroy = {
    }

    void buildKitchen(){
        KitchenSink.withTransaction {
            println "BootStrap inserting 100 customers"
            KitchenSink.repo.createKitchenSinks(10)
        }
    }

}
