/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileDynamic

//see http://plugins.grails.org/plugin/grails/spring-security-appinfo
// for app that shows app-info
@CompileDynamic
class SecurityTestsController {

    def error500() {
        throw new RuntimeException("simulate unknown error")
    }

    def error400() {
        render "how to simulate 400"
    }

    //this is secured and should throw 401 which means user not logged in yet
    def error401() {
        render "should show 401 unless authorized"
    }

    //user is logged in but does not have access to this url
    def error403() {
        render "403 forbidden shold reject"
    }

}
