import gpbench.basic.CityBasic

//import groovy.transform.CompileStatic

//@GrailsCompileStatic
class Loader {
    String dataBinder

    String insertRow(Map row) {
        //CityBasic.repository.create(row, [dataBinder:dataBinder])
        CityBasic.repo.create(row)
    }
}

new Loader(dataBinder: dataBinder)

