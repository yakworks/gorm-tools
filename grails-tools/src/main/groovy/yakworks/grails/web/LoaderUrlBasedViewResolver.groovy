/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.grails.web

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.AbstractUrlBasedView
import org.springframework.web.servlet.view.UrlBasedViewResolver

/**
 * Uses Springs ViewResolver design concepts. The primary lookup uses {@link ViewResourceLocator}
 * The main DispatcherServlet spins through and calls the ViewResolvers ViewResolver.resolveViewName(String viewName, Locale locale)
 * The inheritance chain here is FreeMarkerViewResolver -> AbstractTemplateViewResolver -> UrlBasedViewResolver -> AbstractCachingViewResolver
 * AbstractCachingViewResolver holds the resolveViewName() which calls createView() then loadView() and buildView()
 *
 * This uses the {@link ViewResourceLocator} to locate the resource
 *
 * This gets used simply by registering it as a spring bean
 *   jasperViewResolver(JasperViewResolver){
 *      viewResourceLocator = ref("viewResourceLocator")
 *      jdbcDataSource = ref("dataSource")
 *      reportDataKey = "datasource"
 *      viewNames = ["*.jasper","*.jrxml"] as String[]
 *      viewClass = JasperReportsMultiFormatView.class
 *      order = 20
 *   }
 *
 * @author Joshua Burnett
 */
@Slf4j
@CompileStatic
public class LoaderUrlBasedViewResolver extends UrlBasedViewResolver {
    //injected autowired
    ResourceLoader viewResourceLoader
    Closure buildViewClosure
//    @Override //Overriden for logging
//    public View resolveViewName(String viewName, Locale locale) throws Exception {
//        log.debug("resolveViewName with $viewName")
//
//        return super.resolveViewName(viewName,locale);
//    }

    /**
     * AbstractCachingViewResolver calls this if it doesn't get a hit on the cache
     * Most of the calls to this will look like "/reports/xxx.xyz" etc..
     */
    @SuppressWarnings(['CatchException', 'ReturnNullFromCatchBlock'])
    @Override
    protected View loadView(String viewName, Locale locale) {
        log.debug("jasper loadview running for ${viewName}, locale  $locale")

        //at this point it matches the pattern provided so it should work, try and find it but don't throw exception if
        //not found just log it
        View view = null
        Resource resource
        try {
            resource = viewResourceLoader.getResource(viewName)
        }
        catch(Exception e) {
            //don't don anything, just move on unless debugging is on
            log.debug("loadView Exception", e)
            return null
        }

        if (resource?.exists()) {
            AbstractUrlBasedView gview = buildView(viewName)
            //set the full URL and the cache can the found resource next time
            gview.setUrl(resource.getURL().toString())
            View wrapView = (AbstractUrlBasedView) applicationContext.autowireCapableBeanFactory.initializeBean(gview, viewName)
            view =  (gview.checkResource(locale) ? wrapView : null)
        }

        return view
    }

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        AbstractUrlBasedView view = (AbstractUrlBasedView) super.buildView(viewName)
        if(buildViewClosure) buildViewClosure.call(view)
        return view
    }

}
