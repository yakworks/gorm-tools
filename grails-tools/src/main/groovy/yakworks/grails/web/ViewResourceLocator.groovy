/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.grails.web

import java.util.concurrent.ConcurrentLinkedQueue
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.gsp.GroovyPageResourceLoader
import org.grails.io.support.GrailsResourceUtils
import org.grails.plugins.BinaryGrailsPlugin
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ContextResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.UrlResource
import org.springframework.util.ResourceUtils

import grails.plugins.GrailsPlugin
import grails.plugins.GrailsPluginManager
import grails.plugins.PluginManagerAware
import grails.util.GrailsWebUtil

/**
 * Extended because the handy methods we want in DefaultGroovyPageLocator are protected.
 *
 * Used to locate View resources whether in development or WAR deployed mode from static
 * resources, custom resource loaders and binary plugins.
 * Loads from a local grails-app folder for dev and from WEB-INF in
 * development mode.
 *
 * GroovyPageResourceLoader bean exists in dev/test mode and deals with plugin paths, inplace etc... will not be here in deplyed war
 * @see GroovyPageResourceLoader
 * GroovyPagesGrailsPlugin is where the original beans are setup, take a look at the source to see how they are setup
 * @see org.grails.plugins.web.GroovyPagesGrailsPlugin
 *
 * Used @see org.grails.gsp.io.DefaultGroovyPageLocator heavily as a starting point
 *
 * @author Joshua Burnett
 */

@Slf4j
@CompileStatic
class ViewResourceLocator implements ResourceLoader, ResourceLoaderAware,
        ApplicationContextAware, PluginManagerAware {

    Boolean searchBinaryPlugins = true //keep at false for grails2, true for grails3

    //static final String PATH_TO_WEB_INF_VIEWS = "/WEB-INF/grails-app/views"
    static final String GRAILS_APP_VIEWS_PATH = "/grails-app/views"
    static final String PLUGINS_PATH = "/plugins/"
    String webInfPrefix = "/WEB-INF/"

    List<String> grailsViewPaths = ["/WEB-INF/grails-app/views"]

    Collection<ResourceLoader> grailsResourceLoaders = new ConcurrentLinkedQueue<ResourceLoader>()
    GrailsPluginManager pluginManager
    ApplicationContext ctx

    /**
     * the search locations to try first.
     * Will be classpaths and external directories usually
     */
    List<String> searchPaths = ["classpath:templates/"]

    /**
     * collection of resourceLoaders to use right after searchPaths is scanned
     */
    List<ResourceLoader> searchLoaders

    //Collection<String> initialResourceLoaders = new ConcurrentLinkedQueue<String>();

    /**
     * This is the nuclear approach
     */
    Boolean scanAllPluginsWhenNotFound = true

    ResourceLoader devResourceLoader

    void addSearchPath(String path) {
        if(searchPaths == null) searchPaths = []
        searchPaths.add(path)
    }

    /**
     * adds a resourceLoader to the GrailResourceLoader
     */
    @Override //ResourceLoaderAware
    void setResourceLoader(ResourceLoader resourceLoader) {
        addResourceLoader(resourceLoader)
    }

    void addResourceLoader(ResourceLoader resourceLoader) {
        if (resourceLoader != null && !grailsResourceLoaders.contains(resourceLoader)) {
            grailsResourceLoaders.add(resourceLoader)
        }
    }

    @Override //ApplicationContextAware
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        addResourceLoader(applicationContext)
        ctx = applicationContext

    }

    @Override //PluginManagerAware
    void setPluginManager(GrailsPluginManager pluginManager) {
        this.pluginManager = pluginManager
    }

    @PostConstruct
    @CompileDynamic
    void init(){
        devResourceLoader = ctx.groovyPageResourceLoader
        if(devResourceLoader) grailsResourceLoaders.add(devResourceLoader)
        //setup defaults if not done during bean setup time
//        if(devmode){
//            grailsViewPaths = ["/grails-app/views"]
//        }
    }

    /**
     * Find a view resource for a path. For example /foo/bar.xyz will search for
     * /WEB-INF/grails-app/views/foo/bar.xyz in production
     * and grails-app/views/foo/bar.xyz at development time
     *
     * @param uri The uri to search
     * @return The ContextResource with the realtive path
     */
    Resource locate(String uri) {
        log.debug("ViewResourceLocator.locate is trying ['$uri']")
        Resource res

        res = findInSearchLocations(uri)
        if(res?.exists()) return res

        res = findInSearchLoaders(uri)
        if(res?.exists()) return res

        //try normal grails-app/views dir, for example with "/demo/foo.ftl"
        res = findInGrailsViewPaths(uri)
        if(res?.exists()) return res

        //if path starts with "/plugins/"
        if (uri.startsWith(PLUGINS_PATH)){
            res = findResourceWithPluginPath(uri)
            if(res?.exists()) {
                return res
            }
        }

        //try it based on what plugin the current controller resides in
        res = findWithPluginController(uri)
        if(res?.exists()) {
            log.debug("Success locateWithController")
            return res
        }

        //try the classpath for grails 3, for "demo/foo.ftl"
        res = findInClassPath(uri)
        if(res?.exists()) return res

        //go nuclear and scan all the plugin paths,
        // in grails3, I don't see how this would ever get a hit if classpath did not
        log.debug("....going nuclear with findResourceInPlugins $uri")
        //if (!res) {
            //res = scanPluginsForResource(uri)
        //}
        if(!res) log.debug("...FAIL Did not find $uri")
        return res
    }

    /**
     * Attempts to resolve a uri relative to a plugin controller. Finds the right url if its in a plugin
     * Example: if the controller is Demo in the plugin CoolPlugin looks for a view index.xyz then
     * its going to try for "/plugins/foobar-0.1/grails-app/views/demo/index.xyz"
     *
     * @param uri The name
     * @return The Resouce of the template
     */
    //XXX This does not work in grails3
    Resource findWithPluginController(String uri) {
        log.debug("***** Trying findWithPluginController ******")
        HttpServletRequest request = GrailsWebRequest.lookup()?.getCurrentRequest()

        GroovyObject controller = request ? GrailsWebUtil.getControllerFromRequest(request) : null
        //log.debug("findWithPluginController:['${controller?.getClass()?.name}']")
        if(!controller) return null

        GrailsPlugin plugin = pluginManager.getPluginForInstance(controller)

        //just return if it can't find it
        if (!plugin) return null

        Resource res = findResourceInPlugin( plugin, uri)
        if(res?.exists()) log.debug("FOUND with plugin controller:['${res}']")
        return res
    }

    Resource findInSearchLocations(String uri){
        if(!searchPaths) return
        List fullSearchPaths = searchPaths.collect{ String path ->
            concatPaths(path, uri)
        }
        log.debug("looking in fullSearchPaths : ['${fullSearchPaths}]'")
        Resource res = findResource(fullSearchPaths)//findResource(searchPaths)
        if(res?.exists()){
            log.debug("FOUND in searchLocations")
            return res
        }
    }

    Resource findInSearchLoaders(String uri){
        if(!searchLoaders) return
        log.debug("findInSearchLoaders")
        for (ResourceLoader loader : searchLoaders) {
            Resource resource = loader.getResource(uri)
            if (resource?.exists()) return resource
        }
        return null
    }

    /**
     * Looks in grails-app/views with WEB-INF for production and without it for prod
     */
    Resource findInGrailsViewPaths(String uri){
        List fullPathsToViews = grailsViewPaths.collect{ String path -> concatPaths(path, uri) }
        //String fullPathToView = concatPaths(grailsViewsPath, uri)
        log.debug("fullPathsToViews : ${fullPathsToViews} ")

        Resource res = findResource(fullPathsToViews)//findResource(searchPaths)
        if(res?.exists()){
            log.debug("FOUND in findInGrailsViewPaths")
            return res
        }
    }

    /**
     * search on the base classpath
     */
    Resource findInClassPath(String uri) {
        log.debug("***** Trying with classpath ******")
        String curi = "classpath:$uri" as String
        Resource res = findResource([curi])
        if(res?.exists()) {
            log.debug("SUCCESS with classpath:['$curi']")
            return res
        }
        return null
    }

    Resource findResource(String uri) {
        //return findResource(resolveSearchPaths(uri));
        return findResource([uri])
    }

    /**
     * searches the list of paths
     *
     * @return a resource of ViewResource
     */
    Resource findResource(List<String> paths) {
        //ContextResource foundResource = null;
        Resource resource
        log.debug("spinning through [${grailsResourceLoaders.size()}] resourceLoaders with searchPaths: ['${paths}]")

        for (ResourceLoader loader : grailsResourceLoaders) {
            log.debug("trying ResourceLoader [${loader.class}]")
            resource = findResource( loader, paths)
            if (resource?.exists()) break
        }
        return resource
    }

    /**
     * searches the list of paths for a specific loader
     *
     * @param searchPaths list of search paths to search on
     * @return a resource of ViewResource
     */
    Resource findResource(ResourceLoader rloader, List<String> searchPaths) {
        Resource resource
        Resource foundResource
        //log.debug("*****************resource: ['${((GenericWebApplicationContext)rloader).getServletContext().getRealPath("/")}]'")
        for (String path : searchPaths) {
            //log.debug("trying path [$path] - ResourceLoader: [$rloader]"")
            Resource r = rloader.getResource(path)
            //log.debug("returned resource: ['${r}]'")
            if (r?.exists()) {
                log.debug("SUCCESS with path ['$path'] using ResourceLoader:[${rloader.class}]")
                foundResource = new ViewContextResource(r.URI, path)
                break
            }
        }

        return foundResource
    }

    /**
     * The nuclear approach that scans every plugin to find the view
     */
    //Grails 2 only
    @CompileDynamic
    Resource scanPluginsForResource(String uri) {
        //GrailsPluginManager pluginManager = pluginManager
        if (!pluginManager) return null
        log.debug("*** scanning all plugins *****")
        String pluginBuildSettings = GrailsPluginUtils.getPluginBaseDirectories()
        for (GrailsPlugin plugin : pluginManager.getAllPlugins()) {
            log.debug("SEARCHING PLUGIN: [${plugin.name}] for [$uri]")
//            String pluginPath = pluginManager.getPluginPath(plugin.getName());
//            String pluginUri = GrailsResourceUtils.appendPiecesForUri(pluginPath, uri);
//            log.debug("pluginPath:[$pluginPath], pluginUri: ${pluginUri}")
            Resource resource = findResourceInPlugin( plugin, uri)
            if (resource) {
                log.debug("found in binary plugin: pluginPath ${resource}")
                return resource
            }
        }

        return null
    }

    /**
     * if uri starts with "/plugin/" then this attempts to resolve it
     */
    Resource findResourceWithPluginPath(String uri) {
        if (!uri.startsWith(PLUGINS_PATH)) return

        Resource res
        PluginViewPathInfo pathInfo = getPluginViewPathInfo(uri)

        for (GrailsPlugin plugin : pluginManager.getAllPlugins()) {
            log.debug("pathInfo.pluginName:[$pathInfo.pluginName], plugin.fileSystemName: ${plugin.fileSystemName}")
            if(pathInfo.pluginName == plugin.fileSystemName){
                res = findResourceInPlugin(plugin, pathInfo.path)
                if (res) return res
            }
        }
        return res
    }

    /**
     * find the uri in the plugin
     */
    Resource findResourceInPlugin(GrailsPlugin plugin, String uri) {
        //log.debug("xx findResourceInPlugin: ['${uri}']")
        //if its binary, probably a grails 3 plugins
        if (plugin instanceof BinaryGrailsPlugin ) {
            if(!searchBinaryPlugins) return
            Resource resource = findResourceInBinaryPlugin(plugin as BinaryGrailsPlugin, uri)
            if (resource) {
                log.debug("found in binary plugin: pluginPath ${resource}")
                return resource
            }
        } else {
//            Object res = plugin.@pluginDescriptor
//            log.debug("PLUGIN findResource: [${plugin.name}] for getDescriptor exists  [${res?.exists()}]")
//            if(!(res?.exists())) return
//            uri = concatPaths(res.URI.toString(), GRAILS_APP_VIEWS_PATH,uri)
//            log.debug("PLUGIN findResource: [${plugin.name}] for [$uri]")
            uri = concatPaths(plugin.getPluginPath(), GRAILS_APP_VIEWS_PATH, uri)
            //if(warDeployed) uri = concatPaths("/WEB-INF/",uri)
            log.debug("PLUGIN findResource: [${plugin.name}] for [$uri]")
            Resource resource
            if(devResourceLoader){
                resource = devResourceLoader.getResource(uri)
            }else{
                uri = concatPaths('/WEB-INF/', uri)
                resource = findResource(uri)
            }
            //Resource resource = findResource(uri)
            if (resource?.exists()){
                log.debug("FOUND IN PLUGIN : [${plugin.name}] for [$uri]")
                return resource
            }
        }
        return null
    }

    /**
     * this will not work in Grails2.
     * checks the
     */
    //@CompileDynamic
    Resource findResourceInBinaryPlugin(BinaryGrailsPlugin plugin, String uri) {
        File projectDirectory = plugin.projectDirectory
        //if it has a projectDirectory then its inplace exploded multi project builds in grails 3
        log.debug("findResourceInBinaryPlugin: for plugin ['${plugin.name}'], projectDirectory: ['$projectDirectory']")
        if(projectDirectory) {
            log.debug("Binary plugin is exploded")
            String fullUri = concatPaths(projectDirectory.toURI().toString(), GRAILS_APP_VIEWS_PATH, uri)
            log.debug("findResourceInBinaryPlugin - plugin:['${plugin.name}'], URI:['$fullUri']")
            //no just try it again with the full path
            return findResource([fullUri])
        }

        Resource descriptorResource = plugin.binaryDescriptor.getResource()
        assert descriptorResource.exists()
        //the descriptor is in the META-INF so we need to go up 1 level to get to the root
        Resource r = descriptorResource.createRelative(concatPaths("../", uri))
        if (r.exists()) {
            log.debug ("BinaryGrailsPlugin JAR resource found:['${r}']")
            return new ViewContextResource(r.URI, uri)
        }
        //return findResource(["classpath:$uri".toString()])

        return null
    }

    static String concatPaths(String... pieces){
        GrailsResourceUtils.appendPiecesForUri(pieces)
    }

    @Override //ResourceLoader
    Resource getResource(String uri) {
        //if its something like "/file:..." then lop off the "/" prefix
        if( uri.startsWith('/') && ResourceUtils.isUrl(uri.substring(1)) ){
            uri = uri.substring(1)
        }

        //if it starts with file, classpath, etc... then just pass it through to one of the resourceLoaders
        if(ResourceUtils.isUrl(uri)){
            log.debug("isUrl=true so trying  [$uri]")
            return findResource([uri]) //FAST EXIT
        }
        return locate(uri)
    }

    @Override //ResourceLoader
    ClassLoader getClassLoader() {
        return null
    }

    static PluginViewPathInfo getPluginViewPathInfo(String uri) {
        return new PluginViewPathInfo(uri)
    }

    @CompileStatic
    static class PluginViewPathInfo {
        String basePath  //without the "/plugins/ prefix
        String pluginName //the file system name "xxx-plugin-0.1"
        String path //path with out the "/plugins/xxx-plugin-0.1" such as foo/index.ftl

        PluginViewPathInfo(String uri) {
            basePath = uri.substring(PLUGINS_PATH.length(), uri.length())
            int i = basePath.indexOf("/")
            if (i > -1) {
                pluginName = basePath.substring(0, i)
                path = basePath.substring(i, basePath.length())
            }
        }
    }

    /**
     * a simple resource that build from a URI and adds the base path the resource used to find it
     */
    @CompileStatic
    class ViewContextResource extends UrlResource implements ContextResource {
        private String pathWithinContext //the path given to a resourceLoader to find this Resource

        ViewContextResource(URI uri, String pathWithinContext) {
            super(uri)
            this.pathWithinContext = pathWithinContext
        }

        public String getPathWithinContext() {
            return pathWithinContext
        }
    }

    /**
     * FileSystemResource that explicitly expresses a context-relative path
     * through implementing the ContextResource interface.
     */
    @CompileStatic
    static class FileSystemContextResource extends FileSystemResource implements ContextResource {

        FileSystemContextResource(String path) {
            super(path)
        }

        String getPathWithinContext() {
            return getPath()
        }
    }

    // static ViewResourceLocator testInstance(){
    //     ViewResourceLocator instance = new ViewResourceLocator()
    //     GroovyPageResourceLoader srl = new GroovyPageResourceLoader()
    //     srl.setBaseResource(new FileSystemResource("."))
    //     instance.with{
    //         resourceLoader = srl
    //         searchBinaryPlugins = false //whether to look in binary plugins, does not work in grails2
    //         grailsViewPaths = ["/grails-app/views"]
    //         webInfPrefix = ""
    //     }
    //     return instance
    // }
}
