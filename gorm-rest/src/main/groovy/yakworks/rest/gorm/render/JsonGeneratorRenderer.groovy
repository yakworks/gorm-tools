/*
* Copyright 2013 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.render

import groovy.transform.CompileStatic

/**
 * Json Renderer that uses the default groovy2.5 jsonGenerator
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class JsonGeneratorRenderer<T> implements JsonRendererTrait<T> {

    JsonGeneratorRenderer(Class<T> targetType) {
        this.targetType = targetType
    }

}
