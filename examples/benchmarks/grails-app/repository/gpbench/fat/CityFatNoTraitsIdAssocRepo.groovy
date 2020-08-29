package gpbench.fat

import gorm.tools.repository.GormRepo
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import gpbench.SecUtil
import gpbench.fat.CityMethodEvents
import groovy.transform.CompileStatic

//@Transactional
@CompileStatic
class CityFatNoTraitsIdAssocRepo implements GormRepo<CityFatNoTraitsIdAssoc> {

    @RepoListener
    void beforeBind(CityFatNoTraitsIdAssoc dom, Map data, BeforeBindEvent be) {
        data['regionId'] = data['region']['id']
        data['region2Id'] = data['region2']['id']
        data['region3Id'] = data['region3']['id']
        data['countryId'] = data['country']['id']
        data['country2Id'] = data['country2']['id']
        data['country3Id'] = data['country3']['id']
    }
}
