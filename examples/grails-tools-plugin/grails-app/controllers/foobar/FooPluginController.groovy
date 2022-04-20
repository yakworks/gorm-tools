package foobar

class FooPluginController {

    //simple find
    def index() {
        render(view:"index.md")
    }

    def override() {
        render(view:"override.md")
    }

}
