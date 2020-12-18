package yakworks.taskify.domain

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import grails.gorm.transactions.Transactional

@GormRepository
@CompileStatic
class LocationRepo implements GormRepo<Location> {

    @RepoListener
    void beforeValidate(Location loc) {
        //test rejectValue
        if(loc.city == 'LocationRepoVille'){
            rejectValue(loc, 'city', loc.city, 'from.LocationRepo')
        }
    }

}
