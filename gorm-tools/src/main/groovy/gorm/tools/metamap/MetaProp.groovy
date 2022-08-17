/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.MapConstructor
import groovy.transform.ToString

/**
 * Represents a property on a bean or entity.
 * @see groovy.lang.MetaBeanProperty
 */
@EqualsAndHashCode(includes=["name", "type"], useCanEqual=false) //because its used as cache key
@MapConstructor @ToString
@CompileStatic
class MetaProp implements Serializable {
    private static final long serialVersionUID = 1L

    String name
    Class type

    //OpenAPI schema is added if using the schema plugin and the proper oapi.yml is on path.
    Object schema

    MetaProp() {}

    MetaProp(String name, Class type) {
        this.name = name
        this.type = type
    }

    MetaProp(MetaBeanProperty metaBeanProperty) {
        //constructs using the return type
        this(metaBeanProperty.name, metaBeanProperty.getter.returnType)
    }

    static MetaProp of(String name, Class type){ new MetaProp(name, type)}

}
