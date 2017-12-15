package gorm.tools.dao

import groovy.transform.CompileStatic
import javax.persistence.Transient

@CompileStatic
trait MangoDaoEntity {

    @Transient
    static List<String> quickSearchFields = []
}
