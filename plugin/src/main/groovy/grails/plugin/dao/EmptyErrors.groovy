package grails.plugin.dao

import groovy.transform.CompileStatic

//just a concrete errors implementation for the binding errors so we can use it as a placeholder
@CompileStatic
class EmptyErrors extends org.springframework.validation.AbstractBindingResult {

	public EmptyErrors(String objectName) {
		super(objectName)
	}

	String getActualFieldValue(String d) {
		return getObjectName()
	}

	String getTarget() {
		return getObjectName()
	}
}