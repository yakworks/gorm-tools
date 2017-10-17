package daoapp

class BootStrap {

    def init = { servletContext ->
        100.times {
            new Org(name: "Org#$it").persist()
        }
    }
    def destroy = {
    }
}
