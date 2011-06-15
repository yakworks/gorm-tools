package grails.plugin.dao

import org.codehaus.groovy.grails.exceptions.GrailsException
import org.springframework.validation.Errors
import grails.validation.ValidationException
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

/**
* an extension of the default ValidationException so you can pass the entity and the message map
*/
class GormDataException extends ValidationException{
	def entity //the entity that the error occured on
	Map meta //any meta that can be set and passed up the chain for an error
	Map messageMap //map with message info code,orgs and defaultMessage
	def otherEntity //another entity on which error occurred
	DataAccessException dae //the dae if this wraps one
	
	public GormDataException(String msg) {
		super(msg,new EmptyErrors("empty"))
		//messageMap = [code:"validationException",args:[],defaultMessage:msg]
	}

	public GormDataException(String msg, Errors e) {
		super(msg,e)
		messageMap = [code:"validationException",args:[],defaultMessage:msg]
	}
	public GormDataException(Map msgMap, entity, Errors errors) {
		super(msgMap.defaultMessage?.toString() ?: "Save or Validation Error(s) occurred",errors)
		this.messageMap = msgMap
		this.entity = entity
		messageMap.defaultMessage = messageMap.defaultMessage ?: "Save or Validation Error(s) occurred"
	}
	public GormDataException(Map msgMap, entity) {
		super(msgMap.defaultMessage?.toString() ?: "Save or Validation Error(s) occurred",new EmptyErrors("empty"))
		this.messageMap = msgMap
		this.entity = entity
		messageMap.defaultMessage = messageMap.defaultMessage ?: "Save or Validation Error(s) occurred"
	}

}