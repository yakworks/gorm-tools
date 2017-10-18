package daoapp

class BootStrap {

    def init = { servletContext ->
        100.times {
            new Org(name: "Org#$it", revenue: 100*it, isActive: (it%2 == 0), address: new Address(city: "City#$it").persist()).persist()
        }
    }
    def destroy = {
    }
}
