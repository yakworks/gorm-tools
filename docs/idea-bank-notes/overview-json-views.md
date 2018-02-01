
## Overview of json-views.

there are 5 beans setup in json-views
```
jsonApiIdRenderStrategy(DefaultJsonApiIdRenderer)
jsonViewConfiguration(JsonViewConfiguration)
jsonTemplateEngine(JsonViewTemplateEngine, jsonViewConfiguration)
jsonSmartViewResolver(JsonViewResolver, jsonTemplateEngine) {
    templateResolver = bean(PluginAwareTemplateResolver, jsonViewConfiguration)
}
jsonViewResolver(GenericGroovyTemplateViewResolver, jsonSmartViewResolver )
```

`jsonApiIdRenderStrategy(DefaultJsonApiIdRenderer)` - only used if rendering using the "json-api" format. not relevant to us right now

`jsonViewConfiguration(JsonViewConfiguration)` this generates the config needed. Cool idea for future use
see //https://docs.spring.io/spring-boot/docs/1.3.8.RELEASE/reference/html/configuration-metadata.html#configuration-metadata-annotation-processor

`jsonTemplateEngine(JsonViewTemplateEngine, jsonViewConfiguration)` - primary bean to create the JsonViewTemplate in order to convert an object to JSON. it can be used outside of HTTP and can be seen in our gorm-tools Jsonify. This is one of the ones we want to focus on.

`jsonSmartViewResolver(JsonViewResolver, jsonTemplateEngine)` - This is what I think we want to intercept and replace. JsonViewResolver has a @PostConstruct that uses RendererRegistry bean to instantiate and register a JsonViewJsonRenderer.

## Process

read the [Grails Rest renderers guide] first.

- when using `respond` in the controller it's using the RestResponder trait. See source.
- the internalRespond method calls the registry.findContainerRenderer for the mime type and object. See the source for DefaultRendererRegistry.
- when using the json-views plugin, as mentioned above the JsonViewResolver adds a JsonViewJsonRenderer (extends the DefaultViewRenderer). registry finds it and then the code in the RestResponder calls the render method on it.
- this is where we should be able to intercept it and if it finds a view then make sure we are passing it what it needs and if it doesn't find it then do our munging to process it to json.
	- we should be able to pass in the include,exclude, expand etc.. like we do in Jsonify.


The rules for looking up renders should stay the same thus alowing a custom Renderer bean to be registered as mentioned in the [Grails Rest renderers guide]

The rules should be as follows for includes.

Includes and Excludes lookup precedence
1. Look in appConfig
2. in controller?
3. in repository
4. in domain

So App config overrides controller, controller overrides repo, repo overrides domain.

When it comes to whether or not to use the gson file the rule should be roughly the same as json-views. as currently designed now it will fall back and use the `object/_object.gson` by default if it can't find a view.
Instead of falling back to that `object/_object.gson` when it can't find a view we will be changing is fallback and use the include list if we are providing it first. So the gson view will be used if its there.

## References
[Grails Rest renderers guide]

[Grails Rest renderers guide]: http://docs.grails.org/latest/guide/REST.html#renderers

