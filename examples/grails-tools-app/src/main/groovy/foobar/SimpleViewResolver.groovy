package foobar


import groovy.util.logging.Slf4j

import org.springframework.core.io.ResourceLoader
import org.springframework.web.servlet.View
import org.springframework.web.servlet.view.UrlBasedViewResolver

@Slf4j
class SimpleViewResolver extends UrlBasedViewResolver{

    ResourceLoader viewResourceLocator
    boolean cache = false //turn off for dev

    @Override
    public int getOrder() {
        return 10
    }

    @Override
    protected Class<?> getViewClass() {
        return SimpleFileView.class;
    }

    /**@see org.springframework.util.PatternMatchUtils*/
    @Override
    protected String[] getViewNames() {
        ["*.hbr", "*.md", "*.ftl"] as String[]
    }

    @Override
    protected View loadView(String viewName, Locale locale) {
        log.debug("loadview running for ${viewName}, locale  $locale");
        def res = viewResourceLocator.locate(viewName)
        if(res?.exists()){
            def uri = res.getURI().toString()
            log.debug("loadView going for ${uri}");
            SimpleFileView view = buildView(uri);
            view.viewResourceLocator = viewResourceLocator
            view.beanName = viewName
            def result = getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName);
            log.debug("applyLifecycleMethods done");
            return (view.checkResource(locale) ? result : null);
        }else{
            return null
        }
    }

}
