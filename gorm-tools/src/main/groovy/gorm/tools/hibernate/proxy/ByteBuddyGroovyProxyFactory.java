package gorm.tools.hibernate.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyConfiguration;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.proxy.pojo.bytebuddy.ByteBuddyProxyHelper;
import org.hibernate.type.CompositeType;

import static org.hibernate.internal.CoreLogging.messageLogger;

public class ByteBuddyGroovyProxyFactory implements ProxyFactory, Serializable  {

    private static final CoreMessageLogger LOG = messageLogger( ByteBuddyGroovyProxyFactory.class );

    private final ByteBuddyProxyHelper byteBuddyProxyHelper;

    private Class persistentClass;
    private String entityName;
    private Class[] interfaces;
    private Method getIdentifierMethod;
    private Method setIdentifierMethod;
    private CompositeType componentIdType;
    private boolean overridesEquals;

    private Class proxyClass;

    public ByteBuddyGroovyProxyFactory(ByteBuddyProxyHelper byteBuddyProxyHelper) {
        this.byteBuddyProxyHelper = byteBuddyProxyHelper;
    }

    @Override
    public void postInstantiate(
        String entityName,
        Class persistentClass,
        Set<Class> interfaces,
        Method getIdentifierMethod,
        Method setIdentifierMethod,
        CompositeType componentIdType) throws HibernateException {

        this.entityName = entityName;
        this.persistentClass = persistentClass;
        this.interfaces = toArray( interfaces );
        this.getIdentifierMethod = getIdentifierMethod;
        this.setIdentifierMethod = setIdentifierMethod;
        this.componentIdType = componentIdType;
        this.overridesEquals = ReflectHelper.overridesEquals( persistentClass );

        this.proxyClass = byteBuddyProxyHelper.buildProxy( persistentClass, this.interfaces );
    }

    private Class[] toArray(Set<Class> interfaces) {
        if ( interfaces == null ) {
            return ArrayHelper.EMPTY_CLASS_ARRAY;
        }

        return interfaces.toArray( new Class[interfaces.size()] );
    }

    @Override
    public HibernateProxy getProxy(
        Serializable id,
        SharedSessionContractImplementor session) throws HibernateException {
        final ByteBuddyGroovyInterceptor interceptor = new ByteBuddyGroovyInterceptor(
            entityName,
            persistentClass,
            interfaces,
            id,
            getIdentifierMethod,
            setIdentifierMethod,
            componentIdType,
            session,
            overridesEquals
        );

        try {
            final HibernateProxy proxy = (HibernateProxy) proxyClass.getConstructor().newInstance();
            ( (ProxyConfiguration) proxy ).$$_hibernate_set_interceptor( interceptor );

            return proxy;
        }
        catch (NoSuchMethodException e) {
            String logMessage = LOG.bytecodeEnhancementFailedBecauseOfDefaultConstructor( entityName );
            LOG.error( logMessage, e );
            throw new HibernateException( logMessage, e );
        }
        catch (Throwable t) {
            String logMessage = LOG.bytecodeEnhancementFailed( entityName );
            LOG.error( logMessage, t );
            throw new HibernateException( logMessage, t );
        }
    }
}
