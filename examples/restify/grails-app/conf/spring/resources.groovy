import gorm.tools.openapi.OpenApiGenerator

// Place your Spring DSL code here
beans = {

    openApiGenerator(OpenApiGenerator) { bean ->
        bean.lazyInit = true
        apiSrc = 'api-docs/openapi'
        apiBuild = 'api-docs/dist/openapi'
        namespaceList = ['rally']
    }
}
