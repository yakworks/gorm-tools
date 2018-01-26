
## Quick Start Example

To show what Repository data services are all about let’s walk through an example.

#### Domain setup

Lets assume we are setting up a way to track details for this Project. We might setup a couple of domains like this. 

```groovy
//change the default contraints to be nullable:true
gorm.default.constraints = {
    '*'(nullable:true)
}

@GrailsCompileStatic
class Project {
    String name
    String description

    GitHubInfo gitHubInfo 
    
      
}

@GrailsCompileStatic
class GitHubInfo {
    Long repId         //1829344
    String slug         //yakworks/gorm-tools
    String description  //gorm tools for a clean shaved yak
}
```

and lets say we have a map, perhaps that came from a restful json request or some other service, test data etc...

```groovy
params = [
    name: 'gorm-tools',
    gitHubInfo: [
        repId: 1829344,
        slug: 'yakworks/gorm-tools',
    ]
]
```

### Stock Grails Gorm

**Using stock Grails** [Gorm][]{.new-tab} we would probably implement something like the following 
simplified boiler plate design pattern for the **C** in CRUD

```groovy
@GrailsCompileStatic @Transactional
class ProjectService {
    
    Project createNew(Map data){
        def project = new Project()
        project.properties = data
        project.save(failOnError:true) //throw runtime to roll back if error
    }
    
    //.... other imps
}

// elsewhwere, probably in a controller action, we would inject the service and use it to save
@Autowired ProjectService projectService
...
projectService.createNew(params)

```
 or perhaps we would do it with the new Data Services
 
### Using the Repository

**With this Gorm repository plugin**, we have shaved the yak for you and each domain has a Repository automatically 
setup for this pattern. So with this plugin all the boiler plate from above can be replaced with 1 line!

```groovy
// elsewhere, probably in a controller action. 
Project.create(params)
```

Thats it. The `Project.create()` actually delegates to the Default[GormRepo][].create(). The `create` is wrapped in a transaction, creates the intance,
binds the data and defaults to saving with `failOnError:true`. 
Like all transactional methods if the method is called from inside another transaction it will use it
otherwise it creates a new one. 

> :bulb: **Other Repository Domain Methods**  
> You can do the same thing as above for an `update` or `delete`. 
> The details of whats available can be seen in the [GormRepoEntity]{.new-tab} trait or in the [GormRepoEntity source]{.new-tab} and are outlined below

### Testing the Domain

If you used the script to create the domains then the tests will already be in place for us or you can add one manually like so.

```groovy
package testing

import gorm.tools.testing.hibernate.AutoHibernateSpec
import spock.lang.Specification

class ProjectSpec extends AutoHibernateSpec<Project> {
    /** automatically runs tests on persist(), create(), update(), delete().*/
}
```

Notice the absence of test methods? Running with the the mantra of "convetion over configuration" and "intelligent defaults"
`DomainAutoTest` will mock the domain, setup and create the data for you then exercise the domain and the default repository service for you.
We'll see in the next section how to override the automated tests in the DomainAutoTest. 

### Implementing ProjectRepo Service 

The [DefaultGormRepo] that is setup for the Project domain will of course not always be adequate for the business logic.
Again running with the "intelligent defaults but easy to override" mantra we can easily and selectively override the defaults in the repository. 
Lets say we want to do something more advanced during the create such as validate and retrieve info from GitHub. 
Its not recomended to autowire beans into the domains for performance reasons
It can also be tricky and at times fairly messy trying to modify or create domains using gorm's hibernate inspired event methods.
Such as `beforeCreate` inside the Project domain and deal with flushing.

We can abstract out the logic into a ProjectRepo. 

Lets say we wanted to use a service to validate Github repo and retrieve the description on create.
We can add a class to the `grails-app/repository` directory as in the following example.

```groovy
package tracker

@CompileStatic
class ProjectRepo implements GormRepo<Project>{
    
    @Autowired GitHubApi gitHubApi

    //event method used to update the descriptions with the ones in github
    void afterCreate(Project project, Map params){
        Map repoInfo = gitHubApi.getGitRepo(params.gitHubRepo)
        //check that it was found using the slug or repoId
        if (!repoInfo) 
            throw new DataRetrievalFailureException("Github Repo is invalid for ${params.gitHubInfo}")
        
        if(repoInfo.description){
            //force the gitHubRepo.description to be whats in github
            project.gitHubInfo.description = repoInfo.description
            //update project.description to be the same if its null
            project.description = project.description ?: repoInfo.description
            project.persist() //optional
        }
    }
}

// elsewhere, you can call and it will be automatically taken care of
Project.create(params)

```

Events methods and fired spring events are run inside the inherited transaction and as usual an uncaught runtime exception will rollback.
The `persist` methods is another addition to the domains added by the GormRepoEntity trait. In the example above its not really needed as 
the changes will be flushed during the transaction commit. They are here to doc whats happening 
and so that validation failures can be easily seen.

### Testing the ProjectRepo Changes

[DomainAutoTest] already contains default tests for `create`, `update`, `persist`, `delete` methods, that are called from 
repo. So if, for example, any changes were made for create method for ProjectRepo:

```groovy
@Artefact("Repository")
@Transactional
class ProjectRepo extends DefaultGormRepo<Project> {

    @Override
    @CompileDynamic
    Org create(Map params) {
        params.name = "#" + params.name 
        super.create(params)
     }
}
```
then you can override test case to check specific value 

```
class ProjectSpec extends DomainAutoTest<Project> {
    
    void test_create() {
            when:
            D entity = getDomainClass().create(values)
            then:
            entity.id != null
            entity.name[0] == "#"
        }
    
}
```


[RepositoryApi]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/RepositoryApi.html
[GormRepo]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/GormRepo.html
[GormRepo source]: https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/repository/GormRepo.groovy
[DefaultGormRepo]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/DefaultGormRepo.html
[GormRepoEntity]: https://yakworks.github.io/gorm-tools/api/gorm/tools/repository/GormRepoEntity.html
[GormRepoEntity source]: https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/repository/GormRepoEntity.groovy
[Gorm]: http://gorm.grails.org/latest/hibernate/manual/index.html
[DomainException]: https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/grails/plugin/repository/DomainException.groovy
[GormToolsTest]: https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/testing/GormToolsTest.groovy
[GormToolsHibernateSpec]: https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/testing/GormToolsHibernateSpec.groovy
[DomainAutoTest]: https://yakworks.github.io/gorm-tools/api/gorm/tools/testing/DomainAutoTest.html
