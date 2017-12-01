## Introduction

Since we were setting up a bunch of services that looked a lot like the old school Dao's. We figured we would just call them that instead of data services, repository, etc...

**Purpose**

* To provide standardization across our apps for transactional saves using failOnError:true to throw a Runtime Exception without changing config. see persist() method.
* ensure that each call to persist() will create a Transaction if its not already inside of one. See more about the problems that can occur in example below.
* A clean standard way to abstract boiler plate business logic from the controller into a this dao service for databinding maps and JSON. Works somewhat like gorm's new data services. 
* easily allows validation outside of constraints to persistence without needing to modify the domain source. For example, if I have a `Thing` domain in my things plugin and I need to customize the validation logic for an insert in my Fancy-Thing app or perhaps implement events, I can simply create a ThingDao in the Fancy-Thing that overrides the one in the Thing plugin. 
* Problems with doing more advanced persistence that you can in beforeInsert, beforeUpdate, etc with the build in GORM. As mentioned in the gorm docs
> Do not attempt to flush the session within an event (such as with obj.save(flush:true)). Since events are fired during flushing this will cause a StackOverflowError."

    * we ran into a number of problems, as many do, with session management here. A dao service creates simple centralized contract for how to do this without suffering from the session limitations in hibernate/gorm

If you are using envers or cascade saves then we want the saves and updates to be in a transaction by default and a proper thrown error to cause a roll back of all the changes. Not something you get with failOnError:false.

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

## Installation

## Getting Started
