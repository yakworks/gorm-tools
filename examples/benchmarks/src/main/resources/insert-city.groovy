import gorm.tools.repository.RepoLookup

import groovy.transform.CompileStatic

@CompileStatic
class Loader {
    String dataBinder

    String insertRow(Class dclass, Map row) {
        def repo = RepoLookup.findRepo(dclass)
        repo.create(row)
//        GormRepoEntity instance = (GormRepoEntity)dclass.newInstance()
//        insertRow(instance, row)
    }
}

new Loader(dataBinder: dataBinder)

