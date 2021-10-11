/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import yakworks.testify.model.Project
// import yakworks.testify.model.Taskify

// import restify.domain.OrgType
// import taskify.Project
// import taskify.Task

class BootStrap {
    static Random rand = new Random()

    def init = { servletContext ->
        createProjects()

//        def data = new JsonSlurper().parse(new File("../resources/Contacts.json"))
//        data.each{
//            Contact contact = new resttutorial.Contact(it)
//            contact.save(failOnError:true, flush: true)
//        }

    }

    def createProjects(){
        Project.withTransaction {
            (1..50).each {
                def prod = Project.create([
                    id: it,
                    name: "Fooinator-$it",
                    num: "$it",
                    activateDate: "2020-01-01",
                    startDate: "2020-01-01"
                ], bindId: true)
                assert prod.id == it
                // def task = new Taskify(name: "task1-$it",
                //     project: prod)
                //     .persist()
                // def task2 = new Taskify(name: "task2-$it",
                //     project: prod)
                //     .persist()
            }

            (1..5).each {id ->
                def book = new Book(description: "Shrugged$id")
                book.cost = id % 2 == 0 ? (-1.45) : 9.99
                book.name = id % 2 == 0 ? 'Atlas' : "Galt"
                book.id = id
                book.persist(flush: true)
            }

        }
    }
    def destroy = {
    }

    static Date randomDate() {
        Date dateFrom = Date.parse('yyyy-MM-dd', '2016-01-01')
        Range<Date> range = dateFrom..Date.parse('yyyy-MM-dd', '2016-12-31')
        def addPart = new Random().nextInt(range.to - range.from + 1)
        Date dt = dateFrom + addPart
        return dt
    }

    static String randomID() {
        int num = rand.nextInt(49)+1

    }

}
