package yakworks.testify

import yakworks.gorm.testing.model.KitchenSeedData
import yakworks.gorm.testing.model.KitchenSink
import yakworks.rally.testing.RallySeedData

class BootStrap {

    def init = { servletContext ->
        RallySeedData.init()
        RallySeedData.fullMonty()
        buildKitchen()
    }

    def destroy = {
    }

    void buildKitchen(){
        KitchenSink.withTransaction {
            println "BootStrap inserting 100 customers"
            KitchenSeedData.createKitchenSinks(10)
        }
    }

}
