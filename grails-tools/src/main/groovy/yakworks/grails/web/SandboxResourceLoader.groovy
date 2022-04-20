/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.grails.web

import groovy.transform.CompileStatic

import org.grails.core.io.DefaultResourceLocator
import org.grails.core.io.ResourceLocator
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.ContextResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.util.Assert
import org.springframework.util.StringUtils

/**
 * FileSystemResourceLoader capable of understanding a base paths to search in for security
 * see grails PluginPathAwareFileSystemResourceLoader
 * CURRENTLY NOT USED
 */
@CompileStatic
public class SandboxResourceLoader extends FileSystemResourceLoader {

    public static final String WEB_APP_DIRECTORY = "web-app"
    ResourceLocator resourceLocator = new DefaultResourceLocator()

    public void setSearchLocations(Collection<String> searchLocations) {
        resourceLocator.setSearchLocations(searchLocations)
    }

    @Override
    public Resource getResource(String location) {
        Assert.notNull(location, "Location must not be null")
        String pathToUse = StringUtils.cleanPath(location)
        if (!pathToUse.startsWith("/")) {
            pathToUse = "/" + pathToUse
        }
        if (location.startsWith("/")) {
            return getResourceByPath(location)
        }
        else if (location.startsWith(CLASSPATH_URL_PREFIX)) {
            return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader())
        }

        try {
            // Try to parse the location as a URL...
            URL url = new URL(location)
            return new UrlResource(url)
        }
        catch (MalformedURLException ex) {
            // No URL -> resolve as resource path.
            return getResourceByPath(location)
        }

    }

    @Override
    protected Resource getResourceByPath(String path) {
        Resource resource = super.getResourceByPath(path)
        if (resource?.exists()) {
            return resource
        }

        String resourcePath = path
        if (resourcePath.startsWith(WEB_APP_DIRECTORY)) {
            resourcePath = resourcePath.substring("web-app".length(), resourcePath.length())
        }
        Resource res = resourceLocator.findResourceForURI(resourcePath)
        if (res != null) {
            return res
        }
        return new FileSystemContextResource(path)
    }

    /**
     * FileSystemResource that explicitly expresses a context-relative path
     * through implementing the ContextResource interface.
     */
    @CompileStatic
    private static class FileSystemContextResource extends FileSystemResource implements ContextResource {

        public FileSystemContextResource(String path) {
            super(path)
        }

        public String getPathWithinContext() {
            return getPath()
        }
    }
}
