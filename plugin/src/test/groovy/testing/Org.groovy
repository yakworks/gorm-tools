package testing

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@GrailsCompileStatic
class Org {
    //strings
    String name
    String name2
    String secret
    //boolean
    Boolean inactive = false
    //decimal
    BigDecimal amount
    BigDecimal amount2
    //dates
    Date date
    LocalDate locDate
    LocalDateTime locDateTime
    //special
    //Currency currency //FIXME creates and overflow

    //Associations
    OrgType type //type is required
    Location location //belongs to whatever
    OrgExt ext //<- ext belong to org

    static mapping = {
        //id generator:'assigned'
    }

    static List quickSearchFields = ["name"]

    static constraints = {
        name     nullable: false
        name2    nullable: true
        secret   nullable: true, display: false

        inactive nullable: true
        amount   nullable: true
        amount2  nullable: true

        //dates
        date        nullable: true, example: "2018-01-26T01:36:02Z"
        locDate     nullable: true, example: "2018-01-25"
        locDateTime nullable: true, example: "2018-01-01T01:01:01"
        //special
        //currency    nullable: true

        //Associations
        type nullable: false
        location nullable: true
        ext  nullable: true
    }

//    @CompileDynamic
//    static getConfigs(){
//        return {
//            json.includes = '*'
//            json.excludes = ['location']
//            query.quickSearch = ["name", "name2"]
//            audittrail.enabled = false
//            autotest.update = [name:'foo']
//        }
//    }
}
