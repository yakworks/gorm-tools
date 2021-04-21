package yakworks.testify.model

import groovy.transform.CompileStatic

import gorm.tools.model.IdEnum

@CompileStatic
enum KitchenSinkStatus implements IdEnum<KitchenSinkStatus,Integer> {
    Active(1),
    Inactive(2),
    Void(3)

    final Integer id

    KitchenSinkStatus(Integer id) { this.id = id }
}
