package testing

import gorm.tools.repository.GormRepo
import grails.artefact.Artefact
import gorm.tools.repository.events.*

@Artefact("Repository")
class TestTrxRollbackRepo implements GormRepo<TestTrxRollback> {

    @Override
    TestTrxRollback doPersist(Map args, TestTrxRollback entity) {
        args['failOnError'] = args.containsKey('failOnError') ? args['failOnError'] : true
        getRepoEventPublisher().doBeforePersist(this, entity, args)
        entity.save(args)
        getRepoEventPublisher().doAfterPersist(this, entity, args)

        //throws the exception here to test transaction rollback
        throw new RuntimeException()

        return entity

    }

    @Override
    void doRemove(Map args, TestTrxRollback entity) {
        getRepoEventPublisher().doBeforeRemove(this, entity)
        entity.delete(args)
        getRepoEventPublisher().doAfterRemove(this, entity)

        //throws the exception here to test transaction rollback
        throw new RuntimeException()
    }
}
