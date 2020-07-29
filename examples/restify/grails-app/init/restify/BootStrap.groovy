/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import taskify.Project
import taskify.Task

class BootStrap {
    static Random rand = new Random()

    def init = { servletContext ->
        Project.withTransaction {
            (1..50).each {
                def prod = new Project(name: "Fooinator-$it", num: "$it").save(flush: true, failOnError: true)
                def task = new Task(name: "task1-$it",
                    project: prod)
                    .save(flush: true, failOnError: true)
                def task2 = new Task(name: "task2-$it",
                    project: prod)
                    .save(flush: true, failOnError: true)
            }
           // (1..100).each {
           //     def task = new Task(name: "task-$it",
           //         project: Project.load(rand.nextInt(49)))
           //         .save(flush: true, failOnError: true)
           // }
        }
//        def data = new JsonSlurper().parse(new File("../resources/Contacts.json"))
//        data.each{
//            Contact contact = new resttutorial.Contact(it)
//            contact.save(failOnError:true, flush: true)
//        }

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
        int num = rand.nextInt(49)

    }

}
