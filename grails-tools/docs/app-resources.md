
AppResourceLoader provides a consistent and convention based way to lookup File system resources.

## Configuration

### Basic configuration**
```groovy
    
app {
	resources {

		currentTenant = { return [id:1, num:"tenant-name"]}
		
		attachments.location = 'attachments'

		rootLocation = { args ->
			File file = new File("/projectname/rootLocation")
			if(!file.exists()) {
				file.mkdirs()
			}
			return file.canonicalPath
		}
	}
}

```

#### Define resources config root.  
By default, the resources configuration is expected to be under app.resources. However it can be changed by overriding the appResourceLoader bean definition.

```groovy

    appResourceLoader(AppResourceLoader) {bean ->
        resourcesConfigRootKey = "nine.resources"
        bean.autowire = true
    }

```


#### Defining root location

AppResources needs root location directory to be defined and should exist.

The Root Location can be defined as a closure that returns path to the directory as shown below.
 
```groovy
rootLocation = { args ->
    return "/projectname/rootLocation"
}
```

The closure is passed a map as argument containing keys **tenantId** and **tenantSubDomain**.

#### Defining currentTenant
```groovy
    currentTenant = { return [id:1, num:"tenant-name"]}    
```

This is also a closure and can return dynamic value based on some criteria.
The closure can return a map or any object which has **id** and **num** properties.
This values are passed as tenantId and tenantSubDomain when retrieving the value of root location.

#### Attachments

App resources provides utilities for storing and retrieving attachments.

Declare attachments.location as a subdirectory under root location

```groovy
    attachments.location = 'attachments'
```

_**Create an attachment**_
```groovy
    appResourceLoader.createAttachmentFile(Long attachmentId, String name, String extension, data)
```

The data can be either a file a String, or a byte array

**Accessing a directory under root location**
```groovy
    File directory = appResourceLoader.getLocation(key)
```
Here key is the config key, eg (attachments.location or views.location)

#### Temporary Files
App resource service provides helper method to create temporary files.
```groovy
 appResourceLoader.createTempFile() 
```
By default the temporary files are stored in system temp directory.
However location of tempDir can be changed in configuration as shown below.

```groovy
app.resources.tempDir = "/path/to/dir"
```

**Note:** OS takes care of cleaning system temp directory, however if you explicitly specify the location of tempDir then you will need to take care of cleaning up the temp files regularly.


**ConfigKeyAppResourceLoader**

ConfigKeyAppResourceLoader provides ability to load resources from a directory configured as app resource location.

Define ConfigKeyAppResourceLoader as a bean in grails-app/conf/spring/resources.groovy

```groovy

    viewResourceLocator(yakworks.grails.web.ViewResourceLocator) { bean ->
        searchPaths = []
        searchLoaders = [ref("configKeyAppResourceLoader")]

        searchBinaryPlugins = true //whether to look in binary plugins, does not work in grails2
        scanAllPluginsWhenNotFound = false
        
    }

    configKeyAppResourceLoader(ConfigKeyAppResourceLoader) {
        baseAppResourceKey = "views.location"
        appResourceLoader = ref("appResourceLoader")
    }

```

Define views.location app resource directory

```groovy
app {
	resources {
	    views.location = "views"
	}
}
```

Now the views can be stored under root-location/views directory, and it will be picked up.
