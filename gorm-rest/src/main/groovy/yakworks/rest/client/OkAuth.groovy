/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.client

import groovy.transform.CompileStatic

/**
 * FOR TESTING, static holder for testing, NOT meant for multiple threads making changes.
 * Used in OkHttpRestTrait for login
 */
@CompileStatic
class OkAuth {
    public static String TOKEN
    static String getBEARER_TOKEN(){
        "Bearer ${TOKEN}"
    }
}
