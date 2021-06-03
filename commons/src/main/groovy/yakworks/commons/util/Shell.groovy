/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Simple way to run shell scripts and some shell scripts that are helpful to have.
 */
@Slf4j
@CompileStatic
class Shell {

    /**
     * shortcut call to ['sh', '-c', command].execute().text.trim()
     */
    static String exec(String command){
        return ['sh', '-c', command].execute().text.trim()
    }

    static String exec(String command, String workingDir){
        String cmd = "cd $workingDir && $command"
        return ['sh', '-c', cmd].execute().text.trim()
    }

}
