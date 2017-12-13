package daoapp

class BootStrap {

    def init = { servletContext ->
        100.times {
            new Org(name: "Org#$it",
                num: "Org-num#$it",
                revenue: 100 * it,
                isActive: (it % 2 == 0),
                credit: (it % 2 ? 5000 : null),
                refId: it * 200 as Long,
                testDate: (new Date() + it).clearTime(),
                address: new Address(city: "City#$it", testId: it * 3).persist()).persist()
        }
    }

}
