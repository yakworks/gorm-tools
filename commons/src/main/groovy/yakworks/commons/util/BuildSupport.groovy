/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.util

import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.CompileStatic

@CompileStatic
class BuildSupport {

    /**
     * setup gradle to assign gradle.projectDir for the directory of the build to system properties
     * example:
     * subprojects {
     *   plugins.withId('groovy') {
     *     compileGroovy {
     *       groovyOptions.fork = true
     *       groovyOptions.forkOptions.jvmArgs = ['-Dgradle.projectDir=' + project.projectDir.absolutePath]
     *     }
     *   }
     * }
     *
     */
    static String getGradleProjectDir(){
        return System.getProperty("gradle.projectDir")
    }

    static Path getGradleProjectPath(){
        return Paths.get(getGradleProjectDir())
    }

    /**
     * on multiproject builds this returns the gradle.rootProjectDir property
     * example:
     * tasks.withType(Test) {
     *   systemProperty "gradle.rootProjectDir", rootProject.projectDir.absolutePath
     *   systemProperty "gradle.projectDir", project.projectDir.absolutePath
     * }
     *
     */
    static String getGradleRootProjectDir(){
        return System.getProperty("gradle.rootProjectDir")
    }

    static Path getGradleRootProjectPath(){
        return Paths.get(getGradleRootProjectDir())
    }
}
