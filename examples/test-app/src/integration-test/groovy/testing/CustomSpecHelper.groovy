package testing


trait CustomSpecHelper {

    void specificSetupSpec() {
        this.executionOrder.add("CustomSpecHelper.specificSetupSpec")
    }

    void specificSetup() {
        this.executionOrder.add("CustomSpecHelper.specificSetup")
    }

    void specificCleanup() {
        this.executionOrder.add("CustomSpecHelper.specificCleanup")
    }
}
