## AST & Traits & Artefacts - Oh My

Check out the source code from grails and https://github.com/grails/grails-data-mapping

### Traits

See Grails Docs on TraitInjector

org.grails.compiler.injection.TraitInjectionUtils has usefull stuff

org.grails.datastore.gorm.GormEntity is a good example of how its done to enhance the domains. It extends 2 other traits GormValidateable, DirtyCheckable and implements GormEntityApi. Its not applied using a TraitInjector though. Its done with AST in the GormEntityTransformation. This AST is kicked off in one of 2 ways. 

* If the source domain is under the grails-app/domains `org.codehaus.groovy.grails.compiler.gorm.GormTransformer` will pick it up as it marked with `@grails.compiler.ast.AstTransformer` and extends grails.compiler.ast.GrailsArtefactClassInjector. `GrailsAwareInjectionOperation` collects anything in `org.codehaus.groovy.grails.compiler` and `org.grails.compiler`

* Or if outside grails-app/domains and annotated with @grails.gorm.annotation.Entity it does the AST normally.

When grails is run it knows what classes to load as domain artifacts using grails-core/DomainClassArtefactHandler.doIsDomainClassCheck which returns true for any class with an annotation named "Entity" under the "grails." package.

### AST Transformations

A couple of examples can be found in 



### Other notes
if(ClassUtils.isPresent("com.fasterxml.jackson.annotation.JsonIgnoreProperties”)) 
MultiTenant.isAssignableFrom(entity) to test

AstUtils.injectTrait(classNode, classGormEntityTrait)

See GormStaticApi for the meat. 

See org.grails.datastore.mapping.reflect.ClassPropertyFetcher for examples on getting stuff from hierarchy.