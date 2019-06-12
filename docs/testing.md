Gorm-tools provides a set of test helpers to make the testing process more convenient

## Integration testing

### IntegrationSpecHelper

This is a base trait that contains common logic for integration tests.

Test class should extend [Specification]{.new-tab} class explicitly and implement IntegrationSpecHelper:
 
```groovy 
    class SomeSpec extends Specification implements IntegrationSpecHelper
```

Due to the fact that Spock conventional approach with setup/cleanup methods doesn't work with traits,
this helper uses ```@Before``` / ```@After``` JUnit annotations to implement setup and cleanup logic.

As well as setup logic, IntegrationSpecHelper provides a bunch of useful methods which can be used in tests:
  
  1. buildParams - build GrailsParameter map from a given map
  2. flushAndClear - flushes and clears the session cache, the shortcut for [RepoUtil.flushAndClear()]{.new-tab}
  3. flush - flushes the session cache, the shortcut for [RepoUtil.flush()]{.new-tab}
  4. clear - clears the session cache, the shortcut for [RepoUtil.clear()]{.new-tab}
  5. trackMetaClassChanges - start tracking all metaclass changes made after this call, so it can all be undone later

IntegrationSpecHelper provides an ability to execute application-specific setup/cleanup logic simply by chaining it with
some custom trait:

```groovy
    class SomeSpec extends Specification implements IntegrationSpecHelper, CustomSpecHelper
```

CustomSpecHelper may implement the next methods:

  1. specificSetup
  2. specificCleanup
  3. specificSetupSpec
  4. specificCleanupSpec

The behavior of these methods is equivalent to Spock conventional methods

!!! note
    Specific method is executed after the method defined in IntegrationSpecHelper


### ControllerIntegrationSpecHelper

This trait extends **IntegrationSpecHelper** and provides common logic
which can be useful when testing controllers. So there is no need to implement IntegrationSpecHelper explicitly.
For example:

```groovy 
    class SomeSpec extends Specification implements ControllerIntegrationSpecHelper
```

By default it sets up mock request/response pair and injects application context bean. 

ControllerIntegrationSpecHelper contains the next util methods:

  1. getControllerName - can be overridden in the test class to return the controller name.
     This name is appended to the request attributes. Returns null by default value.
     
     Also, can be specified as a property:
     ```groovy
        String controllerName = "Org"
     ```
     
  2. autowire - autowires bean properties of a given controller and returns the controller instance
  3. mockRender - adds mock of the ```render``` method to a metaclass of a given controller
  4. getCurrentRequestAttributes - returns the current request attributes

Util methods from IntegrationSpecHelper are available as well.

!!! note
    In case of using this trait with a custom helper trait, the setup methods will be called in the next order:
    IntegrationSpecHelper -> ControllerIntegrationSpecHelper -> CustomSpecHelper


## Unit testing
   
### DataRepoTest
    
    TODO

### DomainRepoCrudSpec

    TODO

### DomainRepoTest
    
    TODO

### GormToolsSpecHelper
    
    TODO

### GormToolsTest

    TODO
    
### JsonViewSpecSetup

    TODO
    
### MockJdbcIdGenerator

    TODO

### ExternalConfigAwareSpec
The trait makes it possible to load external config during unit tests.
If external-config plugin is installed, the configuration defined in config.locations will be loaded and be made available to unit tests.


[RepoUtil.flushAndClear()]:https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/repository/RepoUtil.groovy#L91
[RepoUtil.flush()]:https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/repository/RepoUtil.groovy#L101
[RepoUtil.clear()]:https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/repository/RepoUtil.groovy#L111
[Specification]:http://spockframework.org/spock/javadoc/1.0/spock/lang/Specification.html
