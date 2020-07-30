/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify.domain

import taskify.Project
import grails.persistence.Entity

@Entity
class Nested {
    String name
    BigDecimal value

    static belongsTo = [Project]

    static constraints = {
        name blank: true, nullable: true, example: "test"
        value nullable: true, example: new BigDecimal(123)
    }
}
