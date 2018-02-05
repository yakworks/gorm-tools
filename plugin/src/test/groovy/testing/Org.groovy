package testing

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

@Entity @GrailsCompileStatic
class Org {
    String name
    Boolean isActive = false
    BigDecimal amount
    BigDecimal amount2
    Location location
    String secondName
    Date date
    String nameFromRepo
    String event

    OrgExt ext

    static mapping = {
        //id generator:'assigned'
    }

    static List quickSearchFields = ["name"]

    static constraints = {
        name blank: false, nullable: false
        isActive nullable: true
        amount nullable: true
        amount2 nullable: true
        date nullable: true
        secondName nullable: true
        nameFromRepo nullable: true
        //location nullable: false
        event nullable: true, blank: false
    }

    @CompileDynamic
    static getConfigs(){
        return {
            json.includes = '*'
            json.excludes = ['location']
            query.quickSearch = ["name", "secondName"]
            audittrail.enabled = false
            autotest.update = [name:'foo']
        }
    }
}
