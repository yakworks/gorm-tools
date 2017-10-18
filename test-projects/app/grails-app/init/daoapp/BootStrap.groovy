package daoapp

class BootStrap {

    def init = { servletContext ->
        100.times {
            new Org(name: "Org#$it", address: new Address(city: "City#$it").persist()).persist()
        }
    }
    def destroy = {
    }
}
