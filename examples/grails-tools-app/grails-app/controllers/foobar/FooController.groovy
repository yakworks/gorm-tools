package foobar

class FooController {

    //simple find
    def index() {
        render(view:"index.md")
    }


    def indexLeadingSlash() {
        render(view:"/foo/index.md")
    }

    def diffViewDir() {
        render(view:"/fooPlugin/override.md")
    }

    def overridenExternalTemplatesPath() {
        render(view:"override.md.ftl")
    }

    def itsInFoobarPlugin() {
        //view exists but its in the plugin
        render(view:"itsInFoobarPlugin.ftl")
    }

    def fooPluginWithArgument(){
        render(view:"itsInFoobarPlugin.ftl", plugin:"foobar-plugin")
    }

    def tags(){
        [:]
    }

    def indexTemplate(){ //doesn't work
        render(template: "/foo/index.md")
    }
}
