package gorm.tools.hibernate.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import groovy.transform.CompileDynamic;
import groovy.transform.CompileStatic;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor;
import org.hibernate.type.CompositeType;

/**
 * Extends ByteBuddyInterceptor to intecept calls to the getMetaClass and for now toString
 */
class ByteBuddyGroovyInterceptor extends ByteBuddyInterceptor{

    ByteBuddyGroovyInterceptor(
            String entityName,
            Class persistentClass,
            Class[] interfaces,
            Serializable id,
            Method getIdentifierMethod,
            Method setIdentifierMethod,
            CompositeType componentIdType,
            SharedSessionContractImplementor session,
            boolean overridesEquals
    ) {

        super(entityName, persistentClass, interfaces, id, getIdentifierMethod, setIdentifierMethod, componentIdType, session, overridesEquals);
    }

    @Override
    public Object intercept(Object proxy, Method thisMethod, Object[] args) throws Throwable {
        String methodName = thisMethod.getName();
        int params = args.length;

        //only do this hacky stuff if its not initialized
        if(isUninitialized()) {
            if (params == 0 && "getMetaClass".equals(methodName)) {
                return InvokerHelper.getMetaClass(this.persistentClass);
            }
            //if its a dynamic check then will come in as getProperty
            else if (params == 1 && "getProperty".equals(methodName) ) {
                String prop = (String) args[0];
                if ("metaClass".equals(prop)) {
                    return InvokerHelper.getMetaClass(this.persistentClass);
                } else if ("id".equals(prop)) {
                    return getIdentifier();
                }
            }
            return super.intercept(proxy, thisMethod, args);
            // else if(methodName == "toString"){
            //     return toString();
            // }
        }
        else {
             return super.intercept(proxy, thisMethod, args);
        }
        // return val;
    }

    @Override
    public String toString() {
        return this.persistentClass.getSimpleName() + "-" + getIdentifier();
    }

}
