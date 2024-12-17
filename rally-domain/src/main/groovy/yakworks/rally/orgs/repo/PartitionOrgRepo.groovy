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
        //XXX DataProblem? dont do this multiple ways, keep a pattern. How is it being done in ArAdjustRepo that we just did.
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
    //XXX Order these methods https://stackoverflow.com/a/1760877
    // should read like a story, move after the public methods that call it
    protected PartitionOrg createFromOrg(Org org) {
        PartitionOrg porg = new PartitionOrg(num: org.num, name: org.name)
        porg.id = org.id
        porg.persist()
    }

    /**
     * updates PartitionOrg of num or name has changed
     */
    protected void updateIfChanged(Org org) {
        //XXX we dont need to be calling the statics, we are in the repo.
        // Also, the exists static on the domain was what was causing all sorts of problems remeber?
        if (PartitionOrg.exists(org.id) && (org.hasChanged('num') || org.hasChanged('name'))) {
            PartitionOrg.query(id: org.id).updateAll(num: org.num, name: org.name)
        }
    }

    /**
     * Creates or updates partition org, if its new org, or if num/name has changed
     */
    void createOrUpdate(Org org) {
        //XXX this check should not be here. This is the repo for this.
        if (org.isOrgType(orgProps.partition.type)) {
            if (org.isNew()) {
                createFromOrg(org)
            } else {
                updateIfChanged(org)
            }
        }
    }

    void removeForOrg(Org org) {
        //XXX this check should not be here.
        // removeById does all this for you already doesn't it? not sure this removeForOrg is even needed
        if (org.isOrgType(orgProps.partition.type)) {
            PartitionOrg porg = PartitionOrg.get(org.id)
            RepoUtil.checkFound(porg, org.id, PartitionOrg.class.simpleName)
            remove(porg)
        }
    }
}
