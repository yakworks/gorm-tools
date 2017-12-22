grails {
//    gorm.default.mapping = {
//        id generator:'gorm.tools.idgen.SpringIdGenerator'
//    }
    gorm.default.constraints = {
        '*'(nullable:true)
    }
}

