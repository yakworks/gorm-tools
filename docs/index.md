<pre style="line-height: normal; background-color:#2b2929; color:#76ff00; font-family: monospace; white-space: pre;">

      ________                                           _.-````'-,_
     /  _____/  ___________  _____                   ,-'`           `'-.,_
    /   \  ___ /  _ \_  __ \/     \          /)     (\       9ci's       '``-.
    \    \_\  (  <_> )  | \/  Y Y  \        ( ( .,-') )    Yak Works         ```
     \______  /\____/|__|  |__|_|  /         \ '   (_/                         !!
            \/                   \/           |       /)           '           !!!
  ___________           .__                   ^\    ~'            '     !    !!!!
  \__    ___/___   ____ |  |   ______           !      _/! , !   !  ! !  !   !!!
    |    | /  _ \ /  _ \|  |  /  ___/            \Y,   |!!!  !  ! !!  !! !!!!!!!
    |    |(  <_> |  <_> )  |__\___ \               `!!! !!!! !!  )!!!!!!!!!!!!!
    |____| \____/ \____/|____/____  >               !!  ! ! \( \(  !!!|/!  |/!
                                  \/               /_(      /_(/_(    /_(  /_(   
            v3.3.2-SNAPSHOT

</pre>

This is a library of tools to help standardize and simplify the service and Restful controller layer business logic for 
domains and is the basis for the [Gorm Rest API plugin](https://yakworks.github.io/gorm-rest-api/){.new-tab}. 
There are 2 primary patterns this library enables as detailed below for Repositories
and Mango ( A mongo query like way to get data with a Map)

## Domain Repository Services
<small>[jump to detailed docs](repository.md)</small>

A repository is a [Domain Driven Design][] pattern. Used a a place logic to validate, bind, persist and query data that resides 
either in a database or NoSql (via GORM usually of course).
The design pattern here is a bit similiar to [Spring's Repository pattern](https://docs.spring.io/spring-data/data-commons/docs/current/reference/html/)
and Grails GORM's new [Data Services](http://gorm.grails.org/6.1.x/hibernate/manual/#dataServices) pattern.

### Goals

* **Standardization**: a clean common pattern across our apps for domain service layer logic that 
  reduces boiler plate in both services as well as controllers.
* **Transactional Saves**: every save() or persist() is wrapped in a transaction if one doesn't already exist. 
  This is critical when there are cascading saves and updates.
* **RuntimeException Rollback**: saves or `persist()` always occur with failOnError:true so a RuntimeException is 
  thrown for both DataAccessExceptions as well a validation exceptions.
  This is critical for deeply nested domain logic dealing with saving multiple domains chains.
* **Fast Data Binding Service**: databinding from maps (and thus JSON) has to be fast 
  and therefore maps and json are a first class citizen where it belongs in the data service layer instead of the controller layer. 
  Eliminates boiler plate in getting data from the database to Gorm to JSON Map then back again.
* **Events & Validation**: the Repository allows a central place to do events such as beforeSave, beforeValidate, etc 
  so as not to pollute the domain class. This pattern makes it easier to keeps the special logic in a transaction as well. 
  Allows validation outside of constraints to persistence without needing to modify the domain source.
* **Events with Flushing**: As mentioned in the Gorm docs, "Do not attempt to flush the session within an event 
  (such as with obj.save(flush:true)). Since events are fired during flushing this will cause a StackOverflowError.". 
  Putting the event business logic in the Repository keeps it all in a normal transaction and a flush is perfectly fine.  
* **Override Plugin's Domain Logic**: Since the Repository is a service this also easily allows default logic in a provided 
  plugin to be overriden in an application. For example, I may have a CustomerRepo in a plugin that deals with deault common 
  logic to validate address. I can then implement a CustomerRepo in an application and it will override the spring bean
  just as it does for a service. 


## Mango Query

The primary motive here is to create an easy dynamic map based way to query any Gorm Datastore (SQL or NoSQL). 
Using a simple map that can come from json, yaml, groovy config etc... 
A huge motivating factor being able is to be able to have a powerful and flexible way to query using json from a REST 
based client without having to use GraphQL (the only other clean alternative)
The Repositories and RestApiController come with a `query(criteriaMap, closure)` method. It allows you to get a paginated 
list of entities restricted by the properties in the `criteriaMap`.

* A lot of inspiration was drawn from [Restdb.io](https://restdb.io/docs/querying-with-the-api)
* the query language is similar to [Mongo's](https://docs.mongodb.com/manual/reference/operator/query/)
* and CouchDB's new [Mango selector-syntax](http://docs.couchdb.org/en/latest/api/database/find.html#selector-syntax).
* Also inspired by [json-sql](https://github.com/2do2go/json-sql/)

> :memo: 
Whilst selectors have many similarities with MongoDB query documents, 
these arise more from a similarity of purpose and do not necessarily extend to commonality of function or result.

**Example**
for example, sending a JSON search param that looks like this
``` js
{
  "name": "Bill%",
  "type": "New",
  "age": {"$gt": 65}
}
```
would get converted to the equivalent criteria

```javascript
criteria.list {
    ilike "name", "Bill%"
    eq "type", "New"
    gt "age", 65
}
```

## Getting started

To use the Gorm-Tools add the dependency on the plugin to your build.gradle file:

```
runtime "org.grails.plugins:gorm-tools:@VERSION@"
```

And you can start using the plugin by calling the repository methods on domain classes. 
The plugin adds several persistence methods to domain classes. Which delegates to repository classes. This includes persist(), create(params), update(update), remove()

See [Repository](repository.md) for more details

