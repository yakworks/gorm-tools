## Introduction

Since we were setting up a bunch of services that looked a lot like the old school Dao's. We figured we would just call them that instead of data services, repository, etc...

**Purpose**

* To provide standardization across our apps for transactional saves using failOnError:true to throw a Runtime Exception without changing config. see persist() method.
* ensure that each call to persist() will create a Transaction if its not already inside of one. See more about the problems that can occur in example below.
* A clean standard way to abstract boiler plate business logic from the controller into a this dao service for databinding maps and JSON. Works somewhat like gorm's new data services. 
* easily allows validation outside of constraints to persistence without needing to modify the domain source. For example, if I have a `Thing` domain in my things plugin and I need to customize the validation logic for an insert in my Fancy-Thing app or perhaps implement events, I can simply create a ThingDao in the Fancy-Thing that overrides the one in the Thing plugin. 
* Problems with doing more advanced persistence that you can in beforeInsert, beforeUpdate, etc with the build in GORM. As mentioned in the gorm docs
> Do not attempt to flush the session within an event (such as with obj.save(flush:true)). Since events are fired during flushing this will cause a StackOverflowError."

    * we ran into a number of problems, as many do, with session managment here. A dao service creates simple centraolized contract for how to do this without suffering from the session limitations in hibernate/gorm

If you are using envers or cascade saves then we want the saves and updates to be in a transaction by default and a proper thrown error to cause a roll back of all the changes. Not something you get with failOnError:false.

**Example of the transaction propogation issue:** 

With the cascade save of an association where we were saving a Parent with new Child. The issue will kick in  when new Child saved and blew up and the Parent changes stay. We have a good example of this issue in the demo-app under test

With this plugin and a controller you can just do:

```groovy
def update(){
  try{
    def result = YourDomainClass.update(p)
      flash.message = result.message
      redirect(action: 'show', id: result.entity.id)
  }catch(DomainException e){
    flash.message = e.messageMap
    render(view: 'edit', model: [(domainInstanceName): e.entity])
  }
}
```
    
Each domain gets injected with its own static dao object based on the GormDaoSupport service. If it finds a service that in the form of <<Domain Name>>Dao that is in any services or dao dir under grai-app then it will use that.

**Example** You can setup your own dao for the domain like so and keep the logic in your Dao service and leave the controller alone as all the logic will flow over

See [GormDaoSupport](https://github.com/9ci/grails-dao/blob/master/dao-plugin/src/groovy/grails/plugin/dao/GormDaoSupport.groovy)

```
class OrgDao extends GormDaoSupport{ 
    def domainClass = Org
    
    def update(params){
        ... do some stuff to the params
        def result = super.update(params)
        ... do something like log history or send emai with result.entity which is the saved org
        return result
    }
}
```

## Installation

## Getting Started

## Domain Traits
    
### Dynamic methods added to the domains

Every domain gets a dao which is either setup for you or setup by extending e [GormDaoSupport](https://github.com/9ci/grails-dao/blob/grails3/dao-plugin/src/main/groovy/grails/plugin/dao/GormDaoSupport.groovy). Each method is transactional to prevent incomplete cascading saves as exaplained above.

**persist()**: calls the dao.save which in turn calls the dao.save(args) and then domain.save(failOnError:true) with any other args passed in. ex: someDomain.persist(). Throws a [DomainException](https://github.com/9ci/grails-dao/blob/master/dao-plugin/src/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong 

**remove()**:  calls the dao.delete which calls the dao.remove(args) which in turn calls the domain.delete(flush:true) by defualt. Throws a [DomainException](https://github.com/9ci/grails-dao/blob/master/dao-plugin/src/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong 

### Statics added to the domain

**insertAndSave(params)**:  calls the dao.insert which does the bolier plate code you might find in a scaffolded controller. creates a new instance, sets the params and calls the dao.save (esentially the persist()). **ex:** Book.insertAndSave([name:'xyz',isbn:'123']) Throws a [DomainException](https://github.com/9ci/grails-dao/blob/master/dao-plugin/src/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong

**update(params)**:  calls the dao.update which does the bolier plate code you might find in a scaffolded controller. gets the instance base in the params.id, sets the params and calls the dao.save for it. **ex:** Book.update([id:11,name:'aaa']) Throws a (DomainException)[https://github.com/9ci/grails-dao/blob/master/dao-plugin/src/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong 

**remove(params)**:  calls the dao.delete gets the instance base in the params.id, calls the delete for it. **ex:** Book.remove([id:11]) Throws a [DomainException](https://github.com/9ci/grails-dao/blob/master/dao-plugin/src/groovy/grails/plugin/dao/DomainException.groovy) if anything goes wrong 

**dao**: a quick way to get to the dao for the Domain. It will return the stock dao that was created from GormDaoSupport or the Dao you created for the domain.

## DaoUtil and DaoMessage

See [DaoUtil](https://github.com/9ci/grails-dao/blob/grails3/dao-plugin/src/main/groovy/grails/plugin/dao/DaoUtil.groovy)

#### DaoUtil:

**checkFound(entity, Map params,String domainClassName)** checks does the entity exists, if not throws DomainNotFoundException with human readable error text

**checkVersion(entity,ver)** checks the passed in version with the version on the entity (entity.version) make sure entity.version is not greater, throws DomainException

**flush()** flushes the session

**clear()** clears session cache

**flushAndClear()** flushes the session and clears the session cache

#### DaoMessage contains bunch of help methods for creating text messages

See [DaoMessage](https://github.com/9ci/grails-dao/blob/grails3/dao-plugin/src/main/groovy/grails/plugin/dao/DaoMessage.groovy)

## Grails 3:
Dynamic methods were implemented with trait instead of meta programing, so now `@CompileStatic` can be used.
Due to this changes static method `insert` for domain objects was renamed to `insertAndSave`, because domain class instances
already have `insert` method and we can't have both static and instance methods with same list of args.


**Example** To be able to use advantages of the dao plugin for REST apps, extend controller from RestDaoController:

See [RestDaoController](https://github.com/9ci/grails-dao/blob/grails3/dao-plugin/src/main/groovy/grails/plugin/dao/RestDaoController.groovy)

```
class OrgController extends RestDaoController<Org> {
    static responseFormats = ['json']
    static namespace = "api"

    OrgController() {
        super(Org)
    }
}
```

If controller is extended for RestDaoController then methods will use dao services for current domain. For example
POST action will call dao insert method for Org domain.

## Intelligent search 

`search(params, closure)` method was added to GormDaoSupport service. It allows to get list of entities restricted by
properties. 

**Example**

```
Org.dao.search([criteria: [name: "Nam%", type: "New"], max: 20]) {gt "id", 5}
```

The same result can be reached with criteria:

```
Criteria criteria = Org.createCriteria()
criteria.list(max: 20) {
    like "name", "Nam%"
    eq "type", "New"
    gt "id", 5
}
```

So we can specify just parameters in criteria block, and if any specific restriction is needed it can be added
with closure

Bellow will be a list of supported syntax for params in json format, which is supported:

{
    criteria: {
      "ponum":"abc", /* if its a single value eq is default, if it contains % then it uses ilike */
      "reconciled":true, /* boolean */
      "tranDate":"2012-04-23T00:00:00.000Z", /* date */
      "customer.id":101, 
      "customerId":101, /*works in the same way as `customer.id":101` */
      "customer":{"id":101}, /* or object way */
      "or":{ /*TODO: works only if it is one in `criteria`, and currently only on first level*/
        "customer.name":["ilike()","wal%"],
        "customer.num":["ilike()","wal%"]
      },
      "docType":["PA","CM"], /* an array means it will use in/inList */  
      "docType":["in()",["PA","CM"]], /* the above ins would be a short cut for this*/
      "tranType.id":["not in()",[1,2,3]],/* will translate to "not{ in("tranType.id",[1,2,3])]" */
      "refnum":["ilike()","123%"], /* a case-insensitive 'like' expression */
      "refnum":["like()","123%"], /* equivalent to SQL like expression */
      "amount":["between()",0,100], /* between value */
      "oldAmount":["gt()","origAmount"], /* greater than value */
      "oldAmount":["ge()","origAmount"], /* greater or equal than value */"
      oldAmount":["lt()","origAmount"], /* less than value */
      "oldAmount":["le()","origAmount"], /* less or equal than value */
      "amount":["ne()",50], /*not equal*/
      "status.id":[1,2,3], /* an array means it will use in/inList */
      "status":[{"id":1},{"id":2},{"id":3}], /* an array means it will use in/inList */
      "status":["isNull()"], /* translates to isNull*/
    },
  "order":[{"tranDate":"ASC"},{"customer.name","desc"}]
}

**Quick Search**

Quick search - ability to search by one string in criteria filters against several domain fields.
The list of fields are specified in static property `quickSearchFields`, see bellow:

```groovy
class Org {
	String name
    Address address

    static quickSearchFields = ["name", "address.city"]
    ...

```
So intelligent search will add `%` automatically, if quick search string doesn't have it and will apply `ilike` statement
for each field in `quickSearchFields`.

```groovy
Org.dao.search([criteria: [quickSearch: "abc"], max: 20])

```
```groovy
Criteria criteria = Org.createCriteria()
criteria.list(max: 20) {
    or {
        ilike "name", "abc%"
        ilike "address.city", "abc%"
    }
}
```
Keep in mind that quickSearch has higher priority then regular search fields, and if params are 
`[criteria: [quickSearch: "abc", id: 5]]`, then `id` restriction will be ignored