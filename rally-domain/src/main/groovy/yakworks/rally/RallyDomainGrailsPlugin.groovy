/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally

import grails.plugins.Plugin

@SuppressWarnings('Indentation')
class RallyDomainGrailsPlugin extends Plugin {

    def loadAfter = ['boot-security']

    Closure doWithSpring() { {->
        //NOTE: nothing here, in rally-domain we use package scanning and the @Service and @Component.
    }}

}
