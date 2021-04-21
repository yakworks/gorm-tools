package yakworks.testify.model

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener

@GormRepository
@CompileStatic
class AddressRepo implements GormRepo<Address> {

    @RepoListener
    void beforeValidate(Address loc) {
        //test rejectValue
        if(loc.city == 'LocationRepoVille'){
            rejectValue(loc, 'city', loc.city, 'from.LocationRepo')
        }
    }

}
