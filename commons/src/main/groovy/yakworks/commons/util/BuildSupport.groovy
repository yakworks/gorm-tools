package yakworks.commons.util

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
     * @return
     */
    static String getGradleProjectDir(){
        return System.getProperty("gradle.projectDir", '')
    }
}
