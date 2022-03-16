import gorm.tools.openapi.OpenApiGenerator

// Place your Spring DSL code here
beans = {

    //FIXME currently not working here, works in rcm-api
    openApiGenerator(OpenApiGenerator) { bean ->
        bean.lazyInit = true
        apiSrc = 'examples/restify/src/api-docs'
        apiBuild = 'examples/restify/build/api-docs'
        namespaceList = ['rally']
    }
}
