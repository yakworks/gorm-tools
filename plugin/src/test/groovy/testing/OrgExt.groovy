package testing

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity @GrailsCompileStatic
class OrgExt {
    static belongsTo = [org:Org]

    String aNullableString
    String aNotNullableString

    static mapping = {
        id generator:'foreign', params:[property:'org']
        org insertable: false, updateable: false , column:'id'
    }
    static constraints = {
        aNullableString nullable: true
        aNotNullableString nullable: false
    }
}
