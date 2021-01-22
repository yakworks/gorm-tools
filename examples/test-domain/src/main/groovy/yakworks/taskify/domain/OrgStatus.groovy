package yakworks.taskify.domain

import groovy.transform.CompileStatic

import gorm.tools.model.IdEnum

@CompileStatic
enum OrgStatus implements IdEnum<OrgStatus,Integer> {
    Active(1),
    Inactive(2),
    Void(3)

    final Integer id

    OrgStatus(Integer id) { this.id = id }
}
