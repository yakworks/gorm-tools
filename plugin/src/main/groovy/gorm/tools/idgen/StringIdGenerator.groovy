package gorm.tools.idgen

import groovy.transform.CompileStatic

@CompileStatic
interface StringIdGenerator {

    String getNewId(String tranTypeName)

}
