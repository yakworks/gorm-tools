package yakworks.testify.model

import groovy.transform.CompileStatic

import org.springframework.validation.Errors

import gorm.tools.repository.BulkableRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener
import yakworks.rally.job.Job

@GormRepository
@CompileStatic
class AddressRepo implements GormRepo<Address>, BulkableRepo<Address, Job> {

    @RepoListener
    void beforeValidate(Address loc, Errors errors) {
        //test rejectValue
        if(loc.city == 'AddyVille'){
            rejectValue(loc, errors, 'city', loc.city, 'no.AddyVilles')
        }
    }
}
