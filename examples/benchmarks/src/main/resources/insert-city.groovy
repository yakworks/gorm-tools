import gpbench.basic.CityBasic
import org.grails.datastore.gorm.GormEntity

//import groovy.transform.CompileStatic

//@GrailsCompileStatic
class Loader {
    String dataBinder

    String insertRow(Class dclass, Map row) {
        dclass.repo.create(row)
//        GormRepoEntity instance = (GormRepoEntity)dclass.newInstance()
//        insertRow(instance, row)
    }
}

new Loader(dataBinder: dataBinder)

