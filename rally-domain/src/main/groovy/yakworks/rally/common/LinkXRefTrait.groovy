/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common


import groovy.transform.CompileStatic

/**
 * common trait that a concrete composite entity can implement if the stock TagLink will not suffice
 * for example, Org has its own OrgTag lining table
 */
@CompileStatic
trait LinkXRefTrait {

    Long linkedId
    String linkedEntity

    static constraintsMap = [
        linkedId:[ description: 'the id of the entity this tag is linked to', example: 954,
                   nullable: false],
        linkedEntity:[ description: 'The simple name of the linked entity', example: 'ArTran',
                       nullable: false]
    ]
}
