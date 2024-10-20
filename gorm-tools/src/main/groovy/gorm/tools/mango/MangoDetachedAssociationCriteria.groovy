/*
* Copyright 2011 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.finders.FinderMethod
import org.grails.datastore.gorm.query.criteria.AbstractDetachedCriteria
import org.grails.datastore.gorm.query.criteria.DetachedAssociationCriteria
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.query.api.Criteria

import grails.gorm.DetachedCriteria

/**
 * override to elminate the asBoolean
 */
@SuppressWarnings(['ParameterName', 'VariableName', 'InvertedIfElse'])
@CompileStatic
class MangoDetachedAssociationCriteria<T> extends DetachedAssociationCriteria<T> {

    MangoDetachedAssociationCriteria(Class<T> targetClass, Association association) {
        super(targetClass, association)
    }

    MangoDetachedAssociationCriteria(Class targetClass, Association association, String zalias) {
        super(targetClass, association, zalias)
    }

    MangoDetachedAssociationCriteria(Class targetClass, Association association, String associationPath, String zalias) {
        super(targetClass, association, associationPath, zalias)
    }

    /**
     * If the underlying datastore supports aliases, then an alias is created for the given association
     *
     * @param associationPath The name of the association
     * @param zalias The alias
     * @return This create
     */
    @Override //Overriden copy paste in just do we can do instance of this instead
    Criteria createAlias(String associationPath, String zalias) {
        initialiseIfNecessary(targetClass)
        PersistentProperty prop
        if(associationPath.contains('.')) {
            def tokens = associationPath.split(/\./)
            def entity = this.persistentEntity
            for(t in tokens) {
                prop = entity.getPropertyByName(t)
                if (!(prop instanceof Association)) {
                    throw new IllegalArgumentException("Argument [$associationPath] is not an association")
                }
                else {
                    entity = ((Association)prop).associatedEntity
                }
            }
        }
        else {
            prop = persistentEntity.getPropertyByName(associationPath)
        }
        if (!(prop instanceof Association)) {
            throw new IllegalArgumentException("Argument [$associationPath] is not an association")
        }

        Association a = (Association)prop
        DetachedAssociationCriteria associationCriteria = associationCriteriaMap[associationPath]
        if(associationCriteria == null) {
            associationCriteria = new MangoDetachedAssociationCriteria(a.associatedEntity.javaClass, a, associationPath, zalias)
            associationCriteriaMap[associationPath] = associationCriteria
            add associationCriteria
        }
        else {
            associationCriteria.setAlias(zalias)
        }
        return associationCriteria
    }

    /**
     * force an error since this creates unpredictiable truthy checks for criteria
     */
    @Override
    boolean asBoolean(@DelegatesTo(DetachedCriteria) Closure additionalCriteria = null) {
        // (Boolean)withQueryInstance(Collections.emptyMap(), additionalCriteria) { Query query ->
        //     query.projections().count()
        //     ((Number)query.singleResult()) > 0
        // }
        throw new UnsupportedOperationException("Truthy check is not supported, use null check instead")
    }

    @CompileDynamic
    @Override
    def methodMissing(String methodName, Object args) {
        initialiseIfNecessary(targetClass)
        def method = dynamicFinders.find { FinderMethod f -> f.isMethodMatch(methodName) }
        if (method != null) {
            applyLazyCriteria()
            return method.invoke(targetClass, methodName, this, args)
        }

        if (!args) {
            throw new MissingMethodException(methodName, AbstractDetachedCriteria, args)
        }

        final prop = persistentEntity.getPropertyByName(methodName)
        if (!(prop instanceof Association)) {
            throw new MissingMethodException(methodName, AbstractDetachedCriteria, args)
        }


        def zalias = args[0] instanceof CharSequence ? args[0].toString() : null

        def existing = associationCriteriaMap[methodName]
        // NOTE: ONLY CHANGE HERE
        // "!alias && existing" -> !alias && existing != null
        // since DetachedAssociationCriteria inherits from DetachedCriteria and it implements asBoolean
        // then the truthy check on "existing" is running the count query
        //alias = !alias && existing ? existing.alias : alias
        zalias = !zalias && existing != null ? existing.alias : zalias
        DetachedAssociationCriteria associationCriteria = zalias ? new MangoDetachedAssociationCriteria(prop.associatedEntity.javaClass, prop, zalias)
            : new MangoDetachedAssociationCriteria(prop.associatedEntity.javaClass, prop)

        associationCriteriaMap[methodName] = associationCriteria
        add associationCriteria

        def lastArg = args[-1]
        if(lastArg instanceof Closure) {
            Closure callable = lastArg
            callable.resolveStrategy = Closure.DELEGATE_FIRST

            Closure parentCallable = callable
            while(parentCallable.delegate instanceof Closure) {
                parentCallable = (Closure)parentCallable.delegate
            }

            def previous = parentCallable.delegate

            try {
                parentCallable.delegate = associationCriteria
                callable.call()
            } finally {
                parentCallable.delegate = previous
            }
        }
    }

}
