/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic

import gorm.tools.repository.BulkableRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener
import yakworks.rally.job.Job
import yakworks.rally.orgs.model.Company
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@GormRepository
@CompileStatic
class OrgRepo extends AbstractOrgRepo implements BulkableRepo<Org, Job> {

    // add @Override
    @RepoListener
    void beforeValidate(Org org) {
        super.beforeValidate(org)
        verifyCompany(org)
    }

    /**
     * makes sure org has a company on it, and sets it self if its a company
     */
    void verifyCompany(Org org){
        if (org.companyId == null) {
            if (org.type == OrgType.Company){
                org.companyId = org.id
            } else {
                org.companyId = Company.DEFAULT_COMPANY_ID
            }
        }
    }

    /**
     * makes sure the associations are wired to the org
     */
    @Override
    void wireAssociations(Org org) {
        super.wireAssociations(org)
        if (org.calc && !org.calc.id) org.calc.id = org.id
        if (org.member && !org.member.id) {
            org.member.id = org.id
            org.member.org = org //needed for validation
        }
    }
}
