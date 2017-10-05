package grails.plugin.dao

import grails.validation.ValidationException
import groovy.transform.CompileStatic
import org.springframework.validation.Errors

/**
* an extension of the DomainException to be able to handle rest request which should response with 404 error
*/
@CompileStatic
class DomainNotFoundException extends DomainException {

	public DomainNotFoundException(Map msgMap) {
		super(msgMap, null, null, null)
	}

	//Override it for performence improvment, because filling in the stack trace is quit expensive
	@Override
	public synchronized Throwable fillInStackTrace() { }
}