/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import yakworks.rally.testing.RallySeedData

class BootStrap {

    def init = { servletContext ->
        RallySeedData.init()
        RallySeedData.fullMonty()
    }

    def destroy = {
    }


}