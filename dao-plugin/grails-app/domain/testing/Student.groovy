package testing

class Student {
	//static belongsTo = [jumper:Jumper]
	Jumper jumper
	String name
	static daoType = 'transactional'

	static constraints = {
	}
}
