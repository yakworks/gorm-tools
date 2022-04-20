package foobar

import yakworks.grails.web.ViewResourceLocator

class DemoController {

    ViewResourceLocator viewResourceLocator

    def demoView() {
        render viewResourceLocator.locate("/foo/index.md").file.text
    }

    def inplugin() {
        render viewResourceLocator.locate("demo/inplugin.xyz")
    }

    def fooGsp() {
        render view:"fooGsp.gsp"
    }

    def fooGspPlugin() {
        render(view:"fooGsp.gsp", plugin:"foobar-plugin")
    }

    def fooFtl() {
        render view:"foo.ftl"
    }

    def fooFtlPlugin() {
        render(view:"foo.ftl", plugin:"foobar-plugin")
    }

}
