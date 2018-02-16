package gorm.tools.testing.utils

/**
 * Allows dynamic calling of Spock setup/cleanup/setupSpec/cleanupSpec methods on traits without using the JUnit
 * annotations. The goal is to provide a way for the application that benefits from gorm-tools to define some custom
 * test-helper traits, which will not depend on the test-dependencies.
 */
trait SpecHookCaller {

    /**
     * Calls a specificSetup() method on this instance, if it is defined. Does nothing otherwise.
     */
    void doSpecificSetup() {
        invokeIfAvailable('specificSetup')
    }

    /**
     * Calls a specificCleanup() method on this instance, if it is defined. Does nothing otherwise.
     */
    void doSpecificCleanup() {
        invokeIfAvailable('specificCleanup')
    }

    /**
     * Calls a specificSetupSpec() method on this instance, if it is defined. Does nothing otherwise.
     */
    void doSpecificSetupSpec() {
        invokeIfAvailable('specificSetupSpec')
    }

    /**
     * Calls a specificCleanupSpec() method on this instance, if it is defined. Does nothing otherwise.
     */
    void doSpecificCleanupSpec() {
        invokeIfAvailable('specificCleanupSpec')
    }

    /**
     * Invokes a method with a given name and no parameters if it is available on the instance this trait (or its child)
     * is mixed in.
     *
     * @param methodName a name of the target method.
     */
    private def invokeIfAvailable(String methodName) {
        methodAvailable(methodName) ? this."${methodName}"() : null
    }

    /**
     * Checks if a method with a given name is available on the instance this trait (or its child) is mixed in.
     *
     * @param methodName a target name of a method.
     */
    private boolean methodAvailable(String methodName) {
        this.metaClass.respondsTo(this, methodName)
    }
}
