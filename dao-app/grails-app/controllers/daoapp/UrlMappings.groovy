package daoapp

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: "org", action: "index")
        "500"(view:'/error')
        "404"(view:'/notFound')
        "/api/org"(resources: "org", namespace: "api")
    }
}
