package gorm.tools.testing.unit

import spock.lang.Shared

//test to check the overrride methods
class DomainRepoCrudSpecAssertSpec extends DomainRepoCrudSpec<CrudSpecDomain> {

    @Shared boolean testUpdateCalled
    @Shared boolean assertCreateCalled
    @Shared boolean assertPersistCalled
    @Shared boolean assertRemoveCalled

    Map buildCreateMap(Map args) {
        buildMap(firstName: 'create', lastName: 'foo')
    }

    CrudSpecDomain buildPersist(Map args) {
        build(save:false, name: 'persist')
    }

    @Override
    def testUpdate() {
        testUpdateCalled = true
        CrudSpecDomain ent = super.updateEntity(firstName: 'billy', lastName: 'bob')
        assert ent.name == 'billy bob'
        return ent
    }

    boolean assertCreate(ent){
        assertCreateCalled = true
        assert ent.firstName == 'create' && ent.lastName == 'foo'
        assert ent.name == 'create foo'
    }

    boolean assertPersist(CrudSpecDomain ent){
        assertPersistCalled = true
        assert ent.name == 'persist'
    }

    boolean assertRemove(id){
        assertRemoveCalled = true
        assert id instanceof Long
    }

    def cleanupSpec() {
        assert testUpdateCalled
        assert assertCreateCalled
        assert assertPersistCalled
        assert assertRemoveCalled
    }

}



