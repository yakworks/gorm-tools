package gorm.tools.repository

import groovy.transform.CompileStatic
import javax.persistence.Transient

@CompileStatic
trait MangoRepoEntity {

    @Transient
    static List<String> quickSearchFields = []
}
