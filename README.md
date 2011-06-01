A Very Rough Summary
--------
We wanted standardization across our apps with transactional saves and any overriden methods form the controller template. 
We were setting up a bunch of services that looked like Dao's. We figured we should just call them that. 

If you are using envers or cascade saves then we wanted the saves and updates to be in a transaction by default but want the
A good example s a simple cascade save of an association where we were saving a Parent with new Child. The casacded kicked in and new Child saved and blew up saving Parent erroneously leaving the child.

We were also seeing a lot of repetition in code that replaced the actions of a scaffolded controller. Especially the update action
This is what the update action is in the default controller and there is now good way to reuse this or call super.insert()

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

so now to add new stuff you just do

	def update = {
		//easy to add custom code
		try{
			def result = dao.update(params)
			flash.message = message(code: result.message.code, args: result.message.args, default:result.message.default)
			redirect(action: "show", id: result.entity.id)
		}catch(GormException e){
			flash.message = message(code: e.messageMap.code, args: e.messageMap.args, default:e.messageMap.default) 
			render(view: "edit", model: [${propertyName}: e.entity])
		}
    }
	
Or better yet, setup your own dao like so and do the listing in your Dao service and leave the controller alone

	class OrgController {
		def scaffold = Org
		def orgDao //your custom Org dao
		def getDao(){ orgDao }
	}
	
and customize the transactional doa

	class OrgDao extends GormDao{ 
		def domainClass = Org
		
		def update(params){
			... do some stuff to the params
			def result = super.update(params)
			... do something like log history or send emai with result.entity which is the saved org
			return result
		}
	}
	
Setup and install
-------
After installing the plugin you will be able to  create a directory called dao


Usage
------


Examples
--------

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

* 

[LinkThis]: http://www.greenbill.com