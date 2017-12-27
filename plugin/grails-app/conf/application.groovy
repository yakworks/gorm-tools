grails {
//    gorm.default.mapping = {
//        id generator:'gorm.tools.hibernate.PooledTableIdGenerator'
//    }
    gorm.default.constraints = {
        '*'(nullable:true)
    }
}

