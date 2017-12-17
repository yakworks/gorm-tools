package repoapp

class OrgController {
    static scaffold = Org
    def orgDao

    def save() {
        new Org(params).persist()
        def result = orgDao.insert(params)
        redirect(action: "show", id: result.entity.id)
    }
}
