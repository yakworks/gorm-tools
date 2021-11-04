/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import groovy.transform.CompileStatic

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.model.IdEnum

@GrailsCompileStatic
@Entity
class Enummy {
    TestEnum testEnum
    TestEnumIdent enumIdent

    static mapping = {
        id generator:'assigned'
    }

    List getThings() {
        [ Thing.of('val 1'), Thing.of('val 2')]
    }

}

@CompileStatic
enum TestEnum {FOO, BAR}

@CompileStatic
enum TestEnumIdent implements IdEnum<TestEnumIdent,Long> {
    Num2(2), Num4(4)
    final Long id

    TestEnumIdent(Long id) { this.id = id }

    String getNum(){
        "$id-${this.name()}"
    }
}
