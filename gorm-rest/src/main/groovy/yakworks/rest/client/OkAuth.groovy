/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.client

import groovy.transform.CompileStatic

//static holder for testing, NOT meant for multiple threads making changes.
@CompileStatic
class OkAuth {
    public static String BEARER_TOKEN
    public static String TOKEN
}
