// FOR TESTING ONLY, reminder that nothing here get published with jar
grails {
//    gorm.default.mapping = {
//        id generator:'gorm.tools.hibernate.SpringBeanIdGenerator'
//    }
    gorm.default.constraints = {
        '*'(nullable:true)
    }
}

// add some api configs for testing
api {
    paths {
        security {
            sinkExt {
                includes {
                    getCustom = ['id', 'name', 'thing.$stamp' ]
                }
                entityClass = 'yakworks.gorm.testing.model.SinkExt'
            }
        }
    }
}
