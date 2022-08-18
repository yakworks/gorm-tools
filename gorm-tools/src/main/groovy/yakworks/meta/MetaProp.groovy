/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.meta

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.MapConstructor
import groovy.transform.ToString

/**
 * Represents a property on a bean or entity for MetaMap.
 * Basically a MetaBeanProperty but with a property for a schema reference. and
 * @see groovy.lang.MetaBeanProperty
 */
@EqualsAndHashCode(includes=["name", "classType"], useCanEqual=false) //because its used as cache key
@MapConstructor @ToString
@CompileStatic
class MetaProp implements Serializable {
    private static final long serialVersionUID = 1L
    //prop name
    String name
    //java class for prop
    Class classType
    //--- Optional schema props that can be filled in for display and reporting. this is a subset of whats in openapi schema. ---
    // String title //display title
    // number, integer, boolean, array, object, string (this includes dates and files)
    // String type //basic type.

    //OpenAPI schema is added if using the schema plugin and the proper oapi.yml is on path.
    Object schema

    MetaProp() {}

    MetaProp(String name, Class type) {
        this.name = name
        this.classType = type
    }

    MetaProp(MetaBeanProperty metaBeanProperty) {
        //constructs using the return type
        this(metaBeanProperty.name, metaBeanProperty.getter.returnType)
    }

    static MetaProp of(String name, Class type){ new MetaProp(name, type)}

}
