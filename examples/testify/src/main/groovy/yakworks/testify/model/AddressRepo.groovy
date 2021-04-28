package yakworks.testify.model

import groovy.transform.CompileStatic

import org.springframework.validation.Errors

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener

@GormRepository
@CompileStatic
class AddressRepo implements GormRepo<Address> {

    @RepoListener
    void beforeValidate(Address loc, Errors errors) {
        //test rejectValue
        if(loc.city == 'AddyVille'){
            rejectValue(loc, errors, 'city', loc.city, 'no.AddyVilles')
        }
    }

}
