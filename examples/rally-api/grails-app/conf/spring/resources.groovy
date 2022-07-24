import gorm.tools.openapi.OpenApiGenerator
import yakworks.rest.renderer.ApiResultsRenderer

// Place your Spring DSL code here
beans = {
    apiResultsRenderer(ApiResultsRenderer)
    openApiGenerator(OpenApiGenerator) { bean ->
        bean.lazyInit = true
        apiSrc = 'api-docs/openapi'
        apiBuild = 'api-docs/dist/openapi'
        namespaceList = ['rally']
    }
}
