###Purpose


To proved standardization across our apps for transactional saves with failOnError:true. 
It also provides a clean standard way to abstract boiler plate code from the controller as well as proving Restful actions similiar to what is in the new 2.3 versions of grails. 
Since we were setting up a bunch of services that looked a lot like the old school Dao's. We figured we should just call them that. 

If you are using envers or cascade saves then we want the saves and updates to be in a transaction by default and a proper thrown error to cause a roll back of all the changes. Not something you get with failOnError:false.

**Example of the issue:** With the cascade save of an association where we were saving a Parent with new Child. The issue will kick when new Child saved and blew up but the Parent changes stay. We have a good example if this issue in the demo-app under test

###Keeping it dry

We were also seeing a lot of repetition in code that replaced the actions of a scaffolded controller. Especially the update action
This is what the update action is in the default controller and there is now good way to reuse the core logic

	def update = {
       def ${propertyName} = ${className}.get(params.id)
       if (${propertyName}) {
           if (params.version) {
               def version = params.version.toLong()
               if (${propertyName}.version > version) {
                   <% def lowerCaseName = grails.util.GrailsNameUtils.getPropertyName(className) %>
                   ${propertyName}.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: '${domainClass.propertyName}.label', default: '${className}')] as Object[], "Another user has updated this ${className} while you were editing")
                   render(view: "edit", model: [${propertyName}: ${propertyName}])
                   return
               }
           }
           ${propertyName}.properties = params
           if (!${propertyName}.hasErrors() && ${propertyName}.save(flush: true)) {
               flash.message = "\${message(code: 'default.updated.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), ${propertyName}.id])}"
               redirect(action: "show", id: ${propertyName}.id)
           }
           else {
               render(view: "edit", model: [${propertyName}: ${propertyName}])
           }
       }
       else {
           flash.message = "\${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
           redirect(action: "list")
       }
   }

With this plugin and a controller you can just do:

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
	
Each domain gets injected with its own static dao object based on the GormDaoSupport service. If it finds a service that in the form of <<Domain Name>>Dao that is in any services or dao dir under grai-app then it will use that.

**Example** You can setup your own dao for the domain like so and keep the logic in your Dao service and leave the controller alone as all the logic will flow over

	class OrgDao extends GormDaoSupport{ 
		def domainClass = Org
		
		def update(params){
			... do some stuff to the params
			def result = super.update(params)
			... do something like log history or send emai with result.entity which is the saved org
			return result
		}
	}

	
###Dynamic methods added to the domains

Every domain gets a dao which is either setup for you or setup by extending GormDaoSupport. See https://github.com/9ci/grails-dao/blob/master/dao-plugin/src/groovy/grails/plugin/dao/GormDaoSupport.groovy

**persist()**: calls the dao.save which in turn calls the dao.save(args) and then domain.save(failOnError:true) with any other args passed in. ex: someDomain.persist(). Throws a DomainException if anything goes wrong https://github.com/9ci/grails-dao/blob/master/dao-plugin/src/groovy/grails/plugin/dao/DomainException.groovy

**remove()**:  calls the dao.delete which calls the dao.remove(args) which in turn calls the domain.delete(flush:true) by defualt Throws a DomainException if anything goes wrong https://github.com/9ci/grails-dao/blob/master/dao-plugin/src/groovy/grails/plugin/dao/DomainException.groovy

### Statics added to the domain

**insert(params)**:  calls the dao.insert which does the bolier plate code you might find in a scaffolded controller. creates a new instance, sets the params and calls the dao.save (esentially the persist()). **ex:** Book.insert([name:'xyz',isbn:'123'])

**update(params)**:  calls the dao.update which does the bolier plate code you might find in a scaffolded controller. gets the instance base in the params.id, sets the params and calls the dao.save for it. **ex:** Book.update([id:11,name:'aaa'])

**remove(params)**:  calls the dao.delete gets the instance base in the params.id, calls the delete for it. **ex:** Book.remove([id:11])

**dao**: a quick way to get to the dao for the Domain. It will return the stock dao that was created from GormDaoSupport or the Dao you created for the domain.

###DaoUtil and DaoMessage

see TODO after code reorg


More Examples
=====
...

TODOs
--------

* take a look at the code in the RestfulController in grails and see what we can do to keep thing similiar
* move the docs to something other than a README

