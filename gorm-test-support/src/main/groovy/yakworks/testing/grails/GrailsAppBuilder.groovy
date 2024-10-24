/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.grails

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.config.PropertySourcesConfig
import org.grails.spring.context.support.GrailsPlaceholderConfigurer
import org.grails.spring.context.support.MapBasedSmartPropertyOverrideConfigurer
import org.grails.testing.GrailsApplicationBuilder
import org.grails.transaction.TransactionManagerPostProcessor
import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.beans.factory.config.ConstructorArgumentValues
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.AnnotationConfigUtils
import org.springframework.context.support.ConversionServiceFactoryBean
import org.springframework.context.support.StaticMessageSource
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.env.ConfigurableEnvironment

import gorm.tools.ConfigDefaults
import grails.core.GrailsApplication
import grails.core.support.proxy.DefaultProxyHandler
import grails.util.Holders

/**
 * Copied in from grails-testing-support Created by jameskleeh on 5/31/17.
 * Adds the following,
 * - override to use AnnotationConfigApplicationContext
 * -
 */
@CompileStatic
class GrailsAppBuilder extends GrailsApplicationBuilder {

    //HACK, whether to create context.'annotation-config'() being needed in the registerBeans
    //The order is strange and if we dont do annotation-config when using DataRepoTest then we get the following injection failure
    //bean with name 'constraintRegistry': Unsatisfied dependency expressed through method 'setConstraintFactories' parameter 0;
    // ...NoSuchBeanDefinitionException:
    // ...No qualifying bean of type 'org.grails.datastore.gorm.validation.constraints.factory.ConstraintFactory<?>[]'
    boolean isDataRepoTest = true

    //Treats all @Autowired as @Autowired(required = false)
    @CompileStatic
    static class NotRequiredAutowiredAnnotationBeanPostProcessor extends AutowiredAnnotationBeanPostProcessor{

        NotRequiredAutowiredAnnotationBeanPostProcessor() {
            super()
        }

        @Override
        protected boolean determineRequiredStatus(AnnotationAttributes ann) {
            return false
        }
    }

    @Override
    protected ConfigurableApplicationContext createMainContext(Object servletContext) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext()

        ClassLoader applicationClassLoader = this.class.classLoader
        ConfigurableEnvironment configuredEnvironment = context.getEnvironment()

        ConfigurableBeanFactory beanFactory = context.getBeanFactory()
        //setup injection beans that are required=false
        //createAutoWiredBean(beanFactory)
        //runs registerGrailsAppPostProcessorBean(beanFactory) and AnnotationConfigUtils.registerAnnotationConfigProcessors
        prepareContext(context, beanFactory)

        context.register(TestConfiguration)
        context.scan("yakworks.gorm.config")


        //replace with the one that is required=false
        //context.removeBeanDefinition(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)
        //assert !context.getBeanDefinition(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)
        context.registerBeanDefinition(AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
            BeanDefinitionBuilder
                .rootBeanDefinition(NotRequiredAutowiredAnnotationBeanPostProcessor)
                .addPropertyValue("requiredParameterValue", "false")
                .getBeanDefinition())

        //refresh striggers the context build, all the spring bean closures with run at this point.
        context.refresh()
        context.registerShutdownHook()
        context
    }

    @Override
    //so we can remove the context.'annotation-config'
    @CompileDynamic
    @SuppressWarnings(['UnnecessarySelfAssignment'])
    void registerBeans(GrailsApplication grailsApplication) {

        defineBeans(grailsApplication) { ->
            conversionService(ConversionServiceFactoryBean)

            // "${AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME}"(AutowiredAnnotationBeanPostProcessor) {
            //     requiredParameterValue = false
            // }

            xmlns context: "http://www.springframework.org/schema/context"
            // adds AutowiredAnnotationBeanPostProcessor, CommonAnnotationBeanPostProcessor and others
            // see org.springframework.context.annotation.AnnotationConfigUtils.registerAnnotationConfigProcessors method
            //NOTE: commented out as it conflicts with the overrides to replace bean that has required=false as default
            //XXX when removing the the blow up happens in DataTestSetupSpecInterceptor.configureDataTest
            // this line:
            //   applicationContext.getBean('constraintRegistry', ConstraintRegistry).addConstraint(UniqueConstraint)
            if (isDataRepoTest) {
                context.'annotation-config'()
            }

            proxyHandler(DefaultProxyHandler)
            messageSource(StaticMessageSource)
            transactionManagerAwarePostProcessor(TransactionManagerPostProcessor)
            grailsPlaceholderConfigurer(GrailsPlaceholderConfigurer, '${', grailsApplication.config.toProperties())
            mapBasedSmartPropertyOverrideConfigurer(MapBasedSmartPropertyOverrideConfigurer) {
                grailsApplication = grailsApplication
            }
        }
    }

    @Override
    //overriden so we can add in the ConfigDefaults without messing with doWithConfig
    @CompileDynamic
    protected void registerGrailsAppPostProcessorBean(ConfigurableBeanFactory beanFactory) {

        GrailsApplication grailsApp

        Closure doWithSpringClosure = {
            registerBeans(grailsApp)
            executeDoWithSpringCallback(grailsApp)
        }

        Closure customizeGrailsApplicationClosure = { grailsApplication ->
            grailsApp = grailsApplication
            def cfg = grailsApplication.config
            //Add the defaults in
            cfg.putAll(ConfigDefaults.getConfigMap(false))
            //fire closure if its used
            if (doWithConfig) {
                //call the closure
                doWithConfig.call(cfg)
            }
            // reset flatConfig
            grailsApplication.configChanged()

            Holders.config = grailsApplication.config
        }

        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues()
        constructorArgumentValues.addIndexedArgumentValue(0, doWithSpringClosure)
        constructorArgumentValues.addIndexedArgumentValue(1, includePlugins ?: DEFAULT_INCLUDED_PLUGINS)

        MutablePropertyValues values = new MutablePropertyValues()
        values.add('localOverride', localOverride)
        values.add('loadExternalBeans', loadExternalBeans)
        values.add('customizeGrailsApplicationClosure', customizeGrailsApplicationClosure)

        RootBeanDefinition beandef = new RootBeanDefinition(TestRuntimeGrailsApplicationPostProcessor, constructorArgumentValues, values)
        beandef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)
        beanFactory.registerBeanDefinition("grailsApplicationPostProcessor", beandef)
    }

}
