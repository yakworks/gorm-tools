/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.testing.pogos

import groovy.transform.CompileStatic

@CompileStatic
class Thing {
    Long id
    // address fields
    String name

    static Thing of(Long id, String name){
        return new Thing(id: id, name: name)
    }
}
