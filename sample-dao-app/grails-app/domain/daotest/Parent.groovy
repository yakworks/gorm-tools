package daotest

class Parent {
	
	String name
	Child child
	
	static constraints = {
		child nullable:true
	}
}
