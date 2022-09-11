/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.model

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class ValidationEntity implements RepoEntity<ValidationEntity>{
    String xRequired
    String xMaxSize
    String xMinSize
    Integer xMax
    Integer xMin
    // BigDecimal xScale2
    String xMatches
    String xEmail
    String xNotBlank
    Long xRange
    String xInList

    static constraintsMap = [
        xRequired:[nullable: false],
        xMaxSize:[maxSize: 3],
        xMinSize:[minSize: 3],
        xMax:[max: 3],
        xMin:[min: 3],
        // xScale2:[scale: 2],
        xMatches: [matches: "HI"],
        xEmail: [email: true],
        xNotBlank: [blank: false],
        xRange: [range: (1..3)],
        xInList: [inList: ['a', 'b']]
    ]

}
