
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

Each domain gets injected with its own static dao object based on the GormDaoSupport service. If it finds a service that in the form of <<Domain Name>>Dao that is in any services or dao dir under grai-app then it will use that.

**Example** You can setup your own dao for the domain like so and keep the logic in your Dao service and leave the controller alone as all the logic will flow over

See [GormDaoSupport](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/GormDaoSupport.groovy)

```groovy
class OrgDao extends GormDaoSupport {
    def domainClass = Org

    def update(params){
        // ... do some stuff to the params
        def result = super.update(params)
        // ... do something like log history or send email with result.entity which is the saved org
        return result
    }
}
```

## Domain Traits

### Dynamic methods added to the domains

Every domain gets a dao which is either setup for you or setup by extending e [GormDaoSupport](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/GormDaoSupport.groovy). Each method is transactional to prevent incomplete cascading saves as exaplained above.

**persist()**: calls the dao.save which in turn calls the dao.save(args) and then domain.save(failOnError:true) with any other args passed in. ex: someDomain.persist(). Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

**remove()**:  calls the dao.delete which calls the dao.remove(args) which in turn calls the domain.delete(flush:true) by default. Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

### Statics added to the domain

**insertAndSave(params)**:  calls the dao.insert which does the bolier plate code you might find in a scaffolded controller. creates a new instance, sets the params and calls the dao.save (essentially the persist()). **ex:** Book.insertAndSave([name:'xyz',isbn:'123']) Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

**update(params)**:  calls the dao.update which does the boiler plate code you might find in a scaffolded controller. gets the instance base in the params.id, sets the params and calls the dao.save for it. **ex:** Book.update([id:11,name:'aaa']) Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

**remove(params)**:  calls the dao.delete gets the instance base in the params.id, calls the delete for it. **ex:** Book.remove([id:11]) Throws a [DomainException](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

**dao**: a quick way to get to the dao for the Domain. It will return the stock dao that was created from GormDaoSupport or the Dao you created for the domain.

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
