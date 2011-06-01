<%=packageName ? "package ${packageName}\n" : ''%>
import grails.plugin.dao.GormException
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

class ${className}Controller {
	//def daoFactory
	def dao = AH.application.mainContext.getBean("daoFactory").getDao(${className}) 
	
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [${propertyName}List: ${className}.list(params), ${propertyName}Total: ${className}.count()]
    }

    def create = {
        def ${propertyName} = new ${className}()
        ${propertyName}.properties = params
        return [${propertyName}: ${propertyName}]
    }

    def save = {
		try{
			def result = dao.insert(params)
			flash.message = message(code: result.message.code, args: result.message.args, default: result.message.defaultMessage)
			redirect(action: "show", id: result.entity.id)
		}catch(GormException e){
			//flash.message = e.messageMap
			render(view: "create", model: [${propertyName}: e.entity])
		}
    }

    def show = {
        def ${propertyName} = ${className}.get(params.id)
        if (!${propertyName}) {
            flash.message = "\${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
            redirect(action: "list")
        }
        else {
            [${propertyName}: ${propertyName}]
        }
    }

    def edit = {
        def ${propertyName} = ${className}.get(params.id)
        if (!${propertyName}) {
            flash.message = "\${message(code: 'default.not.found.message', args: [message(code: '${domainClass.propertyName}.label', default: '${className}'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [${propertyName}: ${propertyName}]
        }
    }

    def update = {
		try{
			def result = dao.update(params)
			flash.message = message(code: result.message.code, args: result.message.args, default: result.message.defaultMessage)
			redirect(action: "show", id: result.entity.id)
		}catch(GormException e){
			flash.message = message(code: e.messageMap.code, args: e.messageMap.args) 
			render(view: "edit", model: [${propertyName}: e.entity])
		}
    }

    def delete = {
		try{
			def result = dao.remove(params)
			flash.message = message(code: result.message.code, args: result.message.args, default:result.message.default)
			redirect(action: "list")
		}catch(GormException e){
			flash.message = message(code: e.messageMap.code, args: e.messageMap.args, default:e.messageMap.default) 
			redirect(view: "show", id: params.id)
		}
    }
}
