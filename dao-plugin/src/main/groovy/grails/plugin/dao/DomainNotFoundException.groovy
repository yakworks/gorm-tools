package grails.plugin.dao

import groovy.transform.CompileStatic

/**
* an extension of the DomainException to be able to handle rest request which should response with 404 error
*/
@CompileStatic
class DomainNotFoundException extends DomainException {

	DomainNotFoundException(Map msgMap) {
		super(msgMap, null, null, null)
	}

	//Override it for performance improvement, because filling in the stack trace is quit expensive
	@SuppressWarnings(['SynchronizedMethod'])
	@Override
	synchronized Throwable fillInStackTrace() { }
}