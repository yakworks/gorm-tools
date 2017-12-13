
Dao Artefacts
---
Plugin adds a new artefact type **Dao**. One dao bean is configured for each domain.   
[DefaultGormDao](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/dao/DefaultGormDao.groovy) is configured, If no explicit dao class exists for a domain.

Reference to a dao for given domain class can be easily obtained by calling ```DomainClass.dao``` static method.

**Creating Dao classes**  
Dao classes are put inside grails-app/dao and named as ```DomainNameDao``` (eg ```OrgDao```). 
Plugin will automatically lookup all dao classes and configure them as spring beans.

Dao must either implement [GormDao](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/dao/GormDao.groovy) Trait. Or Extend [DefaultGormDao](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/dao/DefaultGormDao.groovy)
 
Example:
 
 ```groovy
 class OrgDao extends DefaultGormDao<Org> {
     
     void beforeCreate(Org org, Map params) {
        //do some thing before create
      }
      
 }
 ```

**Example of the transaction propagation issue:**

With the cascade save of an association where we were saving a Parent with new Child. The issue will kick in when new Child saved and blew up and the Parent changes stay. We have a good example of this issue in the demo-app under test

With this plugin and a controller you can just do:

```groovy
def update() {
  try{
    def result = YourDomainClass.update(p)
      flash.message = result.message
      redirect(action: 'show', id: result.entity.id)
  } catch(DomainException e) {
    flash.message = e.messageMap
    render(view: 'edit', model: [(domainInstanceName): e.entity])
  }
}
```


## Domain Traits

### Methods added to the domains

Every domain gets a dao which is either setup for you or setup by implementing  [GormDao](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/dao/GormDao.groovy). Each method is transactional to prevent incomplete cascading saves as exaplained above.

**persist()**: calls the dao.save which in turn calls the dao.save(args) and then domain.save(failOnError:true) with any other args passed in. ex: someDomain.persist(). Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

**remove()**:  calls the dao.delete which calls the dao.remove(args) which in turn calls the domain.delete(flush:true) by default. Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

### Statics added to the domain

**create(params)**:  calls the dao.create which does the bolier plate code you might find in a scaffolded controller. creates a new instance, sets the params and calls the dao.save (essentially the persist()). **ex:** Book.insertAndSave([name:'xyz',isbn:'123']) Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

**update(params)**:  calls the dao.update which does the boiler plate code you might find in a scaffolded controller. gets the instance base in the params.id, sets the params and calls the dao.save for it. **ex:** Book.update([id:11,name:'aaa']) Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

**remove(id)**:  calls the dao.removeById gets the instance base in the params.id, calls the delete for it. **ex:** Book.remove([id:11]) Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

**dao**: a quick way to get to the dao for the Domain. It will return the stock dao that was created from GormDaoSupport or the Dao you created for the domain.


## Dao events
Each of the dao can implement any of the methods listed below and they will get called during persistence operation.  
 
**beforeCreate(T instance)** - Called before a new instance is saved, can be used to do custom data binding or initialize the state of domain etc.  
**afterCreate(T instance, Map params)** - Called after the new instance is saved.  
**beforeRemove(T instance)** - Called before an instance is deleted. Can be utilized to cleanup related records etc.  
**afterRemove(T instance)** - After an instance is removed.  
**beforeUpdate(T instance, Map params)** - Called before an instance is updated  
**afterUpdate(T instance, Map params)** - Called after an instance is updated  
**beforePersist(T instance)** - Called every time before an instance is saved.  
**afterPersist(T instance)** - Called every time after an instance is saved.
  
## Dao persistence events  
Dao plugin also fires persistence events which can be subscribed just like Gorm events using @Listener.

Following are the events fired.  
**PreDaoCreateEvent**  
**PostDaoCreateEvent**  
**PreDaoUpdateEvent**  
**PostDaoUpdateEvent**  
**PreDaoPersistEvent**  
**PostDaoPersistEvent**  
**PreDaoRemoveEvent**  
**PostDaoRemoveEvent**  

**Example**  
```groovy


class OrgListener {
   
   @Listener(Org)
   void beforeCreate(PreDaoCreateEvent event) {
      Org org = event.entityObject
      Map params = event.params
      //do some thing with org
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
        @Autowired MapBinder binder
        
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
    @Autowired CustomMapBinder mapBinder
    ...
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

