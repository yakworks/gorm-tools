/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import groovy.transform.CompileStatic

import yakworks.commons.model.IdEnum

@CompileStatic
enum KitchenSinkStatus implements IdEnum<KitchenSinkStatus,Integer> {
    Active(1),
    Inactive(2),
    Void(3)

    final Integer id

    KitchenSinkStatus(Integer id) { this.id = id }
}
