import gpbench.City

//import groovy.transform.CompileStatic

//@GrailsCompileStatic
class Loader {
    String dataBinder

    String insertRow(Map row) {
        //City.repository.create(row, [dataBinder:dataBinder])
        City.repo.create(row)
    }
}

new Loader(dataBinder: dataBinder)

