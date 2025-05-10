/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.hazel

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.stereotype.Component

@Component
@Slf4j
@CompileStatic
class DemoJobService {

    void runJob(Long id){
        log.info("runJob called: $id")
        sleep(2000)
    }
}
