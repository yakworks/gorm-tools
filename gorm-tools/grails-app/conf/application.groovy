// For testing
grails {
//    gorm.default.mapping = {
//        id generator:'gorm.tools.hibernate.SpringBeanIdGenerator'
//    }
    gorm.default.constraints = {
        '*'(nullable:true)
    }
}

gorm.tools.security.user.table = 'Users'
