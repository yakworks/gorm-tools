/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.json

import groovy.json.JsonGenerator
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

/**
 * simple trait to add getters for jsonSlurper and jsonGenerator
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
trait JsonEngineTrait {


    JsonSlurper getJsonSlurper(){
        return JsonEngine.slurper
    }

    JsonGenerator getJsonGenerator(){
        return JsonEngine.generator
    }

}
