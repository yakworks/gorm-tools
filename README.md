![CircleCI](https://img.shields.io/circleci/project/github/yakworks/gorm-tools.svg?longCache=true&style=for-the-badge)
[![9ci](https://img.shields.io/badge/BUILT%20BY-9ci%20Inc-blue.svg?longCache=true&style=for-the-badge)](http://9ci.com)
<img src="https://forthebadge.com/images/badges/built-with-love.svg" height="28">
<img src="https://forthebadge.com/images/badges/gluten-free.svg" height="28">
<img src="https://forthebadge.com/images/badges/fuck-it-ship-it.svg" height="28">

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
         Version: 6.1.6
         
</pre>

[RELEASE NOTES](docs/release-notes.md)

| Guide | API | 
|------|--------|
|[Released Docs](https://yakworks.github.io/gorm-tools/) | [Released Api](https://yakworks.github.io/gorm-tools/api)
|[snapshot](https://yakworks.github.io/gorm-tools/snapshot) | [snapshot](https://yakworks.github.io/gorm-tools/snapshot/api)


```
compile "org.grails.plugins:gorm-tools:6.1.6"
``` 

Gorm-tools allows your Grails/Gorm project to start with a design of best practices that they can customize along the way. 
This brings an opinionated starting point to a Grails/Gorm project but without being locked in.
Developers are free to easily customize, replace and disable these patterns when their opinions differ.

## Overview

This is a library of tools to help standardize and simplify the service and Restful controller layer business logic for 
domains and is the basis for the [Gorm Rest API plugin](https://yakworks.github.io/gorm-rest-api/){.new-tab}. 

Gorm-Tools is the next iteration on the [DAO plugin](https://grails.org/plugin/dao) and has been in use for about 10 years processing millions of transactions per day.

There are 3 primary patterns this library enables as detailed below for Repositories,
Mango ( A mongo/graphql like query way to get gorm entity data with a Map) and
Batch or Bulk inserting and updating with data binding

## Domain Repository Services
<small>[jump to reference](docs/repository/ref.md)</small>

A repository is a [Domain Driven Design](docs/usefulLinks.md#references) pattern. Used a a place logic to validate, bind, persist and query data that resides 
either in a database or NoSql (via GORM usually of course).
The design pattern here is a bit similiar to [Spring's Repository pattern]
and Grails GORM's new [Data Services] pattern.

### Goals

* **Standardization**: a clean common pattern across our apps for domain service layer logic that 
  reduces boiler plate in both services as well as controllers.
* **Transactional Saves**: every save() or persist() is wrapped in a transaction if one doesn't already exist. 
  This is critical when there are cascading saves and updates.
* **RuntimeException Rollback by default**: saves or `persist()` always occur with failOnError:true so a RuntimeException is 
  thrown for both DataAccessExceptions as well a validation exceptions.
  This is critical for deeply nested domain logic dealing with saving multiple domains chains.
* **Events & Validation**: the Repository allows a central place to do events such as beforeSave, beforeValidate, etc 
  so as not to pollute the domain class. This pattern makes it easier to keeps the special logic in a transaction as well. 
  Allows validation outside of constraints to persistence without needing to modify the domain source.
* **Events with Flushing**: As mentioned in the Gorm docs, "Do not attempt to flush the session within an event 
  (such as with obj.save(flush:true)). Since events are fired during flushing this will cause a StackOverflowError.". 
  Putting the event business logic in the Repository keeps it all in a normal transaction and a flush is perfectly fine.  
* **Easy Override/Replace Plugin's Domain Logic**: Since the Repository is a service this also easily allows default logic in a provided 
  plugin to be overriden in an application. For example, I may have a CustomerRepo in a plugin that deals with deault common 
  logic to validate address. I can then implement a CustomerRepo in an application and it will override the spring bean
  just as it does for a service. 

## Fast Data Binder & Batch Insert/Update

We process millions of transactions per day and needed more performant binding performance.

### Goals

* **FAST Data Binding Service**: databinding from maps (and thus JSON) has to be fast. 
  We sacrfice a small amount of functionality for a big performance gain
  Maps and json are a first class citizen in the data service layer instead of the controller layer. 
  Eliminates boiler plate in getting data from the database to Gorm to JSON Map then back again.
* **Asynchronous batch processing PERFORMANCE**: GORM insert and updates can be chunked and processed in parrallel 
  using GPARS or RxJava making it easy to processes millions of records from JSON, CSV or Excel
  
## JSON Query and Filtering (Mango Query)

The primary motive here is to create an easy dynamic map based way to query any Gorm Datastore (SQL or NoSQL). 
Using a simple map that can come from json, yaml, groovy config etc... 
A huge motivating factor being able is to be able to have a powerful and flexible way to query using json from a REST 
based client without having to use *GraphQL* (the only other clean alternative)
The Repositories and RestApiController come with a `query(criteriaMap, closure)` method. It allows you to get a paginated 
list of entities restricted by the properties in the `criteriaMap`.

* A lot of inspiration was drawn from [Restdb.io]
* the query language is similar to [Mongo's]
* and CouchDB's new [Mango selector-syntax] .
* Also inspired by [json-sql]

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
[repository docs]: docs/repository/ref.md
[Spring's Repository pattern]:https://docs.spring.io/spring-data/data-commons/docs/current/reference/html/
[Data Services]:http://gorm.grails.org/6.1.x/hibernate/manual/#dataServices
[Restdb.io]:https://restdb.io/docs/querying-with-the-api
[Mongo's]:https://docs.mongodb.com/manual/reference/operator/query/
[Mango selector-syntax]:http://docs.couchdb.org/en/latest/api/database/find.html#selector-syntax
[json-sql]:https://github.com/2do2go/json-sql/

## Developer Notes

### Running mkdocs locally

Docs are built with https://yakworks.github.io/mkdocs-material-components/
Run 
> ```pip install -r pip-requirements.txt```
And then ```mkdocs serve``` see the docs if you have troubles

### Publishing Plugin Releases

See [Developer Docs](docs/developer.md) for info on our release process

Build are automatically published by travis. 
Snapshots are published from master branch, and releases are published from tags to BinTray.

If you want to publish artifacts from your local system.
 
Define following properties in ~/.gradle/gradle.properties

- bintrayUser
- bintrayKey
- artifactoryUsername
- artifactoryPassword

bintray credentials are used for **bintrayUpload** task. Artifactory credentials are used for publishing snapshots to 9ci artifactory.

**Using latests SNAPSHOT**  

Configure 9ci repo in build.gradle

```groovy
repositories {
  maven { url "http://repo.9ci.com/oss-snapshots" }
 }
```

See [version.properties](version.properties) for snapshot version

```groovy
dependencies {
 compile('org.grails.plugins:gorm-tools:x.y.z-SNAPSHOT') { changing = true } 
}
```
