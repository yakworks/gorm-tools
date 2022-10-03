package yakity

class UrlMappings {

    static mappings = {
        /**
         * NOTE: Whats in UrlMappings should NOT also exist in a Spring Controler's @GetMapping or
         */
        // "/admin"(view:"/admin") if you uncomment this then you will see an error since its also mapped with Spring
        // "/"(view:"/index")
        "/grails"(view:"/grails")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
