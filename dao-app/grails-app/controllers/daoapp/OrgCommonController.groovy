package daoapp

class OrgCommonController {
	def orgDao
    //def index = { }

	def save(){
		new Org(params).persist()
		def result = orgDao.insert(params)
		redirect(action: "show",id: result.entity.id)
	}
}
