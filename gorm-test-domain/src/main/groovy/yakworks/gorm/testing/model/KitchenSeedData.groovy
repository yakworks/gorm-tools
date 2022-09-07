/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model


import groovy.transform.CompileStatic

@CompileStatic
class KitchenSeedData {

    static void createKitchenSinks(int count){
        KitchenSink.repo.createKitchenSinks(count)
    }

}
