package daoapp

class OrgController {
	def scaffold = Org
	def orgDao
    //def index = { }

	def save(){
		def result = orgDao.insert(params)
		redirect(action: "show",id: result.entity.id)
	}
}
