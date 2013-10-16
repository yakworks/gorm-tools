Purpose
--------

To proved standardization across our apps for transactional saves with failOnError:true. 
It also provides a clean standard way to abstract boiler plate code from the controller as well as proving Restful actions similiar to what is in the new 2.3 versions of grails. 
Since we were setting up a bunch of services that looked a lot like the old school Dao's. We figured we should just call them that. 

If you are using envers or cascade saves then we want the saves and updates to be in a transaction by default and a proper thrown error to cause a roll back of all the changes. Not something you get with failOnError:false.

**Example of the issue:** With the cascade save of an association where we were saving a Parent with new Child. The issue will kick when new Child saved and blew up but the Parent changes stay. We have a good example if this issue in the demo-app under test

Keeping it dry
============
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
			def result = domainClass.update(p)
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

	
Dynamic methods added to the domains
========

Apple
:   Pomaceous fruit of plants of the genus Malus in 
    the family Rosaceae.


Restful controller
======


More Examples
=====

Setup a simple dao

	class someDao extends GormDao{
		def domainClass = YourDomainClass
	}

or inject the daoFactory where you want it

	def daoFactory
	....
	def dao = daoFactory.getDao(YourDomainClass)


GOTCHAS and TODOs
--------

* take a look at the code in the RestfulController in grails and see what we can do to keep thing similiar
* 

[LinkThis]: http://www.greenbill.com
