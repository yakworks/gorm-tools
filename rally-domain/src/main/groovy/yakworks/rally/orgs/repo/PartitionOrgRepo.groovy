/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import javax.inject.Inject

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.model.LongIdGormRepo
import grails.gorm.transactions.ReadOnly
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
        throw new UnsupportedOperationException("Creating new PartitionOrg is not allowed")
    }

    @Override
    PartitionOrg update(Map data, PersistArgs args) {
        throw new UnsupportedOperationException("Updating PartitionOrg is not allowed")
    }

    @Override
    void removeById(Serializable id) {
        throw new UnsupportedOperationException("Deleting PartitionOrg is not allowed")
    }

    /**
     * Creates or updates partition org, if its new org, or if num/name has changed
     */
    void createOrUpdate(Org org) {
        if (org.isNew()) {
            createFromOrg(org)
        } else {
            updateIfChanged(org)
        }
    }

    /**
     * Looks up PartionOrg from Org
     */
    @ReadOnly
    PartitionOrg getByOrg(Org org) {
        PartitionOrg.get(org.id)
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
     * updates PartitionOrg if num or name has changed
     */
    protected void updateIfChanged(Org org) {
        if (exists(org.id) && (org.hasChanged('num') || org.hasChanged('name'))) {
            query(id: org.id).updateAll(num: org.num, name: org.name)
        }
    }

    @Override
    PartitionOrg lookup(Map data) {
        PartitionOrg porg
        if(data.num)  {
            porg = PartitionOrg.findWhere(num:data.num)
        }
        return porg
    }

}
