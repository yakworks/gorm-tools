package grails.plugin.dao

import grails.validation.ValidationException
import org.springframework.validation.Errors

/**
* an extension of the DomainException to be able to handle rest request which should response with 404 error
*/
class DomainNotFoundException extends DomainException {

	public DomainNotFoundException(Map msgMap) {
		super(msgMap, null,null,null)
	}
}