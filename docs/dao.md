
## Quick Start Example

To show what DAO Repository Services are all about let’s walk through an example.

#### Domain setup

Lets assume we are setting up a way to track details for this Project. We might setup a couple of domains like this. 

```groovy
//change the default contraints to be nullable:true
gorm.default.constraints = {
    '*'(nullable:true)
}

@GrailsCompileStatic
class Project {
    String name
    String description

    GithubRepo gitHubRepo 
    
      
}

@GrailsCompileStatic
class GitHubRepo {
    Long repoId         //1829344
    String slug         //yakworks/gorm-tools
    String description  //gorm tools for a clean shaved yak
}
```

and lets say we have a map, perhaps that came from a resful json request or some other service, test data etc...

```groovy
params = [
    name: 'gorm-tools',
    gitHubRepo: [
        repoId: 1829344,
        slug: 'yakworks/gorm-tools',
    ]
]
```

### Stock Grails Gorm

**Using stock Grails** [Gorm][]{.new-tab} we would probably implement something like the following 
simplified boiler plate design pattern for the **C** in CRUD

```groovy
@GrailsCompileStatic @Transactional
class ProjectService {
    
    Project createNew(Map data){
        def project = new Project()
        project.properties = data
        project.save(failOnError:true) //throw runtime to roll back if error
    }
    
    //.... other imps
}

// elsewhwere, probably in a controller action, we would inject the service and use it to save
@Autowired ProjectService projectService
...
projectService.createNew(params)

```

### Using the Dao

**With this Gorm repository plugin**, we have shaved the yak for you and each domain has a Repository or Dao automitically 
setup for this pattern. So with this plugin all the boiler plate from above can be replaced with 1 line!

```groovy
// elsewhwere, probably in a controller action. 
Project.create(params)
```

Thats it. The `Project.create()` actually delegates to the Default[GormDao][].create(). The `create` is wrapped in a transaction, creates the intance,
binds the data and defaults to saving with `failOnError:true`. 
Like all transactional methods if the method is called from inside another transaction it will use it
otherwise it creates a new one. 

> :bulb: **Other Dao Domain Methods**  
> You can do the same thing as above for an `update` or `delete`. 
> The details of whats available can be seen in the [DaoEntity]{.new-tab} trait or in the [DaoEntity source]{.new-tab} and are outlined below

### Testing the Domain

If you used the script to create the domains then the tests will already be in place for us or you can add one manually like so.

```groovy
package testing

import gorm.tools.testing.DomainAutoTest
import spock.lang.Specification

class ProjectSpec extends Specification implements DomainAutoTest<Project> {
    /** automatically runs tests on persist(), create(), update(), delete().*/
}
```

Notice the absence of test methods? Running with the the mantra of "convetion over configuration" and "intelligent defaults"
`DomainAutoTest` will mock the domain, setup and create the data for you then exercise the domain and the default dao service for you.
We'll see in the next section how to override the automated tests in the DomainAutoTest. 

### Implementing ProjectDao Service 

The [DefaultGormDao] that is setup for the Project domain will of course not always be adequate for the business logic.
Again running with the "intelligent defaults but easy to override" mantra we can easily and selectively override the defaults in the dao. 
Lets say we want to do something more advanced during the create such as validate and retrieve info from GitHub. 
Its not recomended to autowire beans into the domains for performance reasons
It can also be tricky and at times fairly messy trying to modify or create domains using gorm's hibernate inspired event methods.
Such as `beforeCreate` inside the Project domain and deal with flushing.

We can abstract out the logic into a ProjectDao. 

Lets say we wanted to use a service to validate Github repo and retrieve the description on create.
We can add a class to the `grails-app/dao` directory as in the following example.

```groovy
package tracker

@CompileStatic
class ProjectDao implements GormDao<Project>{
    
    @Autowired GitHubApi gitHubApi

    //event method used to update the descriptions with the ones in github
    void afterCreate(Project project, Map params){
        Map repoInfo = gitHubApi.getRepo(params.gitHubRepo)
        //check that it was found using the slug or repoId
        if (!repoInfo) 
            throw new DataRetrievalFailureException("Github Repo is invalid for ${params.gitHubRepo}")
        
        if(repoInfo.description){
            //force the gitHubRepo.description to be whats in github
            project.gitHubRepo.description = repoInfo.description
            project.gitHubRepo.persist()  //optional 
            //update project.description to be the same if its null
            project.description = project.description ?: repoInfo.description
            project.persist() //optional
        }
    }
}

// elsewhere, you can call and it will be automatically taken care of
Project.create(params)

```

Events methods and fired spring events are run inside the inherited transaction and as usual an uncaught runtime exception will rollback.
The `persist` methods is another addition to the domains added by the DaoEntity trait. In the example above its not really needed as 
the changes will be flushed during the transaction commit. They are here to doc whats happening 
and so that validation failures can be easily seen.

### Testing the Custom ProjectDao

```
class ProjectSpec extends Specification implements DomainAutoTest<Project> {
    
    
}
```

## The Dao Service Artefact

This plugin adds a new artefact type **Dao**. Each domain will have a spring bean setup for it if one doesn't exists already.

A dao bean is configured for each domain with a [DefaultGormDao][]{.new-tab} unless explicit dao class.   
The trait [GormDao][]{.new-tab} implements the [DaoApi][]{.new-tab} interface and is what backs the DefaultGormDao. 
You'll mostly use the [GormDao][]{.new-tab} trait when creating a custom concrete implementation of a Dao.

Reference to a dao for given domain class can be easily obtained by calling `MyDomainClass.dao` static method.

### Implementing Dao Services

If you need to override the [DefaultGormDao][] that is attached to each domain then you can create your own service
inside grails-app/dao and name it ```YourDomainNameDao``` (eg ```OrgDao```). 
Plugin will automatically lookup all dao classes and configure them as spring service beans to be used for your domain.

Dao must either implement [GormDao][] Trait or if you wish extend [DefaultGormDao][]
 
**Example:**
 
 ```groovy
 class OrgDao implements GormDao<Org> {
     
     void beforeCreate(Org org, Map params) {
        //do some thing before create
      }
      
 }
 ```

## DaoEntity Trait

See Groovydocs api for the [DaoEntity][] that is injected onto all domains.

### Instance methods added to the domains

Every domain gets a dao which is either setup for you or setup by implementing 
[GormDao][] 
Each method is transactional and will prevent incomplete cascading saves.

- **persist()**: calls the dao.save which in turn calls the dao.save(args) and then domain.save(failOnError:true) 
  with any other args passed in. ex: someDomain.persist(). 
  Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

- **remove()**:  calls the dao.delete which calls the dao.remove(args) which in turn calls the domain.delete(flush:true) by default. Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

### Statics added to the domain

- **create(params)**:  calls the dao.create which does the bolier plate code you might find in a scaffolded controller. creates a new instance, sets the params and calls the dao.save (essentially the persist()). **ex:** Book.insertAndSave([name:'xyz',isbn:'123']) Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

- **update(params)**:  calls the dao.update which does the boiler plate code you might find in a scaffolded controller. gets the instance base in the params.id, sets the params and calls the dao.save for it. **ex:** Book.update([id:11,name:'aaa']) Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

- **remove(id)**:  calls the dao.removeById gets the instance base in the params.id, calls the delete for it. **ex:** Book.remove([id:11]) Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

- **dao**: a quick way to get to the dao for the Domain. It will return the stock dao that was created from GormDaoSupport or the Dao you created for the domain.


## Dao Events 

### Methods

Each of the dao can implement any of the methods listed below and they will get called during persistence operation.  
 
- **beforeCreate(T instance, Map params)** - Called before a new instance is saved, can be used to do custom data binding or initialize the state of domain etc.  
- **afterCreate(T instance, Map params)** - Called after the new instance is saved.  
- **beforeRemove(T instance)** - Called before an instance is deleted. Can be utilized to cleanup related records etc.  
- **afterRemove(T instance)** - After an instance is removed.  
- **beforeUpdate(T instance, Map params)** - Called before an instance is updated  
- **afterUpdate(T instance, Map params)** - Called after an instance is updated  
- **beforePersist(T instance)** - Called every time before an instance is saved.  
- **afterPersist(T instance)** - Called every time after an instance is saved.
  
### Spring Events

The dao also publishes a number of [events as listed in the Groovydoc API](https://yakworks.github.io/gorm-tools/api/gorm/tools/dao/events/package-summary.html)

**Example**  
```groovy

import org.springframework.context.event.EventListener
import gorm.tools.dao.events.BeforeCreateEvent

class OrgListener {
   
    @EventListener
    void beforeCreate(BeforeCreateEvent<Org> event) {
       Org org = event.entity
       //Do some thing here.
    }
}

```

## Data binding using MapBinder
Plugin comes with a ```MapBinder``` Which is used by Daos to perform databinding.
Plugin configures ```GormMapBinder``` as default implementation of ```MapBinder```. ```GormMapBinder``` is similar to grails data binder in the sense that it uses registered value converters and fallbacks to spring ConversionService.
However GormMapBinder is optimized to convert most commonly encountered property types such as Numbers and Dates without going through the converters, thus resulting in faster performance.

**Example**

```groovy

class SomeService {
        
        @Autowired 
        MapBinder binder
        
        void foo(Map params) {
           Org org = new Org()
           binder.bind(org, params)
        }

}

```

**Using custom MapBinder**  
By default all dao's use the default ```GormMapBinder``` for databinding. However when a dao is explicitely created for a domain class, and if required, a custom MapBinder implementation can be used to perform databinding as per the need.

```groovy

class CustomMapBinder implements MapBinder {

    public <T> GormEntity<T> bind(GormEntity<T> target, Map<String, Object> source, String bindMethod) {
        //implement  
    }

    public <T> GormEntity<T> bind(GormEntity<T> target, Map<String, Object> source) {
        //implement
    }

}

class OrgDao implements GormDao<Org> {
    
    @Autowired
    CustomMapBinder mapBinder
    
    .........   
}

```

This will make the OrgDao use CustomMapBinder for data binding.

## DaoUtil and DaoMessage

See [DaoUtil](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DaoUtil.groovy)

#### DaoUtil:

**checkFound(entity, Map params,String domainClassName)** checks does the entity exists, if not throws DomainNotFoundException with human readable error text

**checkVersion(entity,ver)** checks the passed in version with the version on the entity (entity.version) make sure entity.version is not greater, throws DomainException

**flush()** flushes the session

**clear()** clears session cache

**flushAndClear()** flushes the session and clears the session cache

#### DaoMessage contains bunch of help methods for creating text messages

See [DaoMessage](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DaoMessage.groovy)

## Grails 3:
Dynamic methods were implemented with trait instead of meta programing, so now `@CompileStatic` can be used.
Due to this changes static method `insert` for domain objects was renamed to `insertAndSave`, because domain class instances
already have `insert` method and we can't have both static and instance methods with same list of args.


## DAO Message

See [DaoMessage](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DaoMessage.groovy)

Gorm-tools provides a way to build message maps with information about a status of a domain instance.
Uses i18n messages.

## Saved and no saved messages

The example below shows how to build ```saved``` message for a domain:

```groovy

    User user = new User(id:100,version:1)

    Map msg = DaoMessage.saved(user)
    assert 'default.saved.message' == msg.code //i18 code
    assert 100 == msg.args[1]

```

## List of available messages

* saved
* not saved
* updated
* not updated
* deleted
* not deleted
* notFound
* optimisticLockingFailure - Another user has updated the resource while you were editing

Gorm-tools provides its own types of exceptions to handle errors which relate to domains.

## DomainException
See [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy)

An extension of the default ValidationException. It is possible to pass the entity and the message map.

## DomainNotFoundException
See [DomainNotFoundException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainNotFoundException.groovy)

An extension of the DomainException to be able to handle rest request which should respond with 404 error.


## Async batch processing support
Plugin makes it easy to process list of batches asynchronously with transaction using [AsyncBatchSupport](https://yakworks.github.io/gorm-tools/api/gorm/tools/async/AsyncBatchSupport.html). 
[BparsBatchSupport](https://yakworks.github.io/gorm-tools/api/gorm/tools/async/GparsBatchSupport.html) is default implementation provided by the plugin.


**BatchSize** - Is the batchsize used for slicing the list. The default value is obtained from ```hibernate.jdbc.batch_size``` configuration setting. However it can be explicitely passed in args as shown in below example.  
**poolSize** - Is the size of Gpars thread pool used by ```GparsBatchSupport```. The default value can configured using ```gpars.poolsize```. If not configured, it will use the default poolsize used by Gpars. which is available processors + 1


**Example**:
```groovy

class TestService {
    AsyncBatchSupport asyncBatchSupport

    void insertBatches(List<Map> list) {
        asyncBatchSupport.parallelCollate([batchSize:100], list) { Map record, Map args ->
            Org.create(record)
        }
    }

}

```

The above code snippet will slice the list into batches of 100 and run each batch in parallel and wrap it in transaction. 

The list can be processed in parallel without it being wrapped in transaction using ```asyncBatchSupport.parallel``` method.

```groovy

asyncBatchSupport.parallel(asyncBatchSupport.collate(list)) { List batch, Map args ->
    //do some thing with the batch.
}

```



## Testing support
Plugin provides [DaoDataTest](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/testing/DaoDataTest.groovy) and [DaoHibernateSpec](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/testing/DaoHibernateSpec.groovy)
To make it easy to write tests which utilizes dao.

**Writing unit tests using DaoDataTest**  
DaoDataTest extends grails DataTest and configures a dao bean for every mock domain. The Dao class must exist in same package as the domain class, or else, it will configure DefaultGormDao as the dao for the given domain.
  
```groovy

class CitySpec extends Specification implements DaoDataTest {
   
   void setup() {
     mockDomain(City)
   }
   
   void "test create"() {
     given:
     Map params = [name:"Chicago"]
     
     when:
     City city = City.create(params)
     //or City.dao.create(params)
     
     then:
     city.name == "Chicago"
   }
}
```  

**DaoHibernateSpec**  
DaoHibernateSpec extends HibernateSpec and setups dao beans for domains. Can be used to unit test with full hibernate support with inmemory database.


```groovy

class CitySpec extends DaoHibernateSpec {
   
  List<Class> getDomainClasses() { [City] }
   
   void "test create"() {
     given:
     Map params = [name:"Chicago"]
     
     when:
     City city = City.create(params)
     //or City.dao.create(params)
     
     then:
     city.name == "Chicago"
   }
}
```  

When ```getDomainClasses()``` is overridden DaoHibernateSpec will try to find the dao in the same package as domain class. Alternatively if ```getPackageToScan()``` is provided, it will find all the dao from the given package and below it. 


[DaoApi]: https://yakworks.github.io/gorm-tools/api/gorm/tools/dao/DaoApi.html
[GormDao]: https://yakworks.github.io/gorm-tools/api/gorm/tools/dao/GormDao.html
[GormDao source]: https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/dao/GormDao.groovy
[DefaultGormDao]: https://yakworks.github.io/gorm-tools/api/gorm/tools/dao/DefaultGormDao.html
[DaoEntity]: https://yakworks.github.io/gorm-tools/api/gorm/tools/dao/DaoEntity.html
[DaoEntity source]: https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/dao/DaoEntity.groovy
[Gorm]: http://gorm.grails.org/latest/hibernate/manual/index.html
