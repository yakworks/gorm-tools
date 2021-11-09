/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.model

import groovy.transform.CompileStatic

/**
 * A marker for an entity that is has a name.
 */
@CompileStatic
trait Named {

    String name

}
