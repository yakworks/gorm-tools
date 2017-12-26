package testing

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import grails.artefact.Artefact
import gorm.tools.repository.events.*

@Artefact("Repository")
class Org2Repo implements GormRepo<Org2> {

    @Override
    Org2 doUpdate(Map args, Map data) {
        println "Org2.doUpdate"
        Org2 entity = get(data)
        bindAndSave(args, entity, data, BindAction.Update)

        //throwing exception to test transaction
        throw new RuntimeException()

        return entity
    }
}
