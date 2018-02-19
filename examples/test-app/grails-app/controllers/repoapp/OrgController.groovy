package repoapp

class OrgController {
    static scaffold = Org
    def orgRepo

    def save() {
        new Org(params).persist()
        def result = orgRepo.insert(params)
        redirect(action: "show", id: result.entity.id)
    }

    def renderOrgTemplate() {
        render(plugin: "gorm-tools", template: "orgTemplate")
    }
}
