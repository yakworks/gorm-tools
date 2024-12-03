/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import javax.inject.Inject

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.LongIdGormRepo
import yakworks.api.problem.data.DataProblem
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.PartitionOrg

@GormRepository
@CompileStatic
class PartitionOrgRepo extends LongIdGormRepo<PartitionOrg> {

    @Inject
    OrgProps orgProps

    @Override
    PartitionOrg create(Map data, PersistArgs args) {
        throw DataProblem.ex("Can not create Partition org")
    }

    @Override
    PartitionOrg update(Map data, PersistArgs args) {
        throw DataProblem.ex("Can not update Partition org")
    }

    @Override
    void removeById(Serializable id) {
        throw DataProblem.ex("Can not delete Partition org")
    }

    /**
     * Creates a PartitionOrg for the given org. If org.type is partition org type
     */
    protected PartitionOrg createFromOrg(Org org) {
        PartitionOrg porg = new PartitionOrg(num: org.num, name: org.name)
        porg.id = org.id
        porg.persist()
    }

    /**
     * updates PartitionOrg of num or name has changed
     */
    protected void updateIfChanged(Org org) {
        if (PartitionOrg.exists(org.id) && (org.hasChanged('num') || org.hasChanged('name'))) {
            PartitionOrg.query(id: org.id).updateAll(num: org.num, name: org.name)
        }
    }

    /**
     * Creates or updates partition org, if its new org, or if num/name has changed
     */
    void createOrUpdate(Org org) {
        if (org.isOrgType(orgProps.partition.type)) {
            if (org.isNew()) {
                createFromOrg(org)
            } else {
                updateIfChanged(org)
            }
        }
    }

    void removeForOrg(Org org) {
        if (org.isOrgType(orgProps.partition.type)) {
            PartitionOrg porg = PartitionOrg.get(org.id)
            RepoUtil.checkFound(porg, org.id, PartitionOrg.class.simpleName)
            remove(porg)
        }
    }
}
