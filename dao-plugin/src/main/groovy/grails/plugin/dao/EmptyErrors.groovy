package grails.plugin.dao

//just a concrete errors implementation for the binding errors so we can use it as a placeholder
class EmptyErrors extends org.springframework.validation.AbstractBindingResult{
	public EmptyErrors(String objectName) {
		super(objectName)
	}
	def getActualFieldValue(String d){
		return getObjectName()
	}
	def getTarget(){
		return getObjectName()
	}
}