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
	
	public GormDataException(String msg) {
		super(msg,new EmptyErrors("empty"))
		//messageMap = [code:"validationException",args:[],defaultMessage:msg]
	}

	public GormDataException(String msg, Errors e) {
		this(msg,e,null)
	}
	public GormDataException(String msg, Errors e,Throwable cause) {
		super(msg,e)
		initCause(cause)
		messageMap = [code:"validationException",args:[],defaultMessage:msg]
	}
	
	public GormDataException(Map msgMap, entity, Errors errors) {
		this(msgMap, entity, errors, null)
	}
	public GormDataException(Map msgMap, entity) {
		this(msgMap, entity,null,null)
	}
	public GormDataException(Map msgMap, entity, Throwable cause) {
		this(msgMap, entity,null,cause)
	}
	public GormDataException(Map msgMap, entity, Errors errors, Throwable cause) {
		super(msgMap.defaultMessage?.toString() ?: "Save or Validation Error(s) occurred",errors ?: new EmptyErrors("empty"))
		initCause(cause)
		this.messageMap = msgMap
		this.entity = entity
		messageMap.defaultMessage = messageMap.defaultMessage ?: "Save or Validation Error(s) occurred"
	}

}