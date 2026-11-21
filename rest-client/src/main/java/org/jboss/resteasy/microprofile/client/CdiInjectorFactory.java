package org.jboss.resteasy.microprofile.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.jboss.resteasy.core.InjectorFactoryImpl;
import org.jboss.resteasy.microprofile.client.logging.LogMessages;
import org.jboss.resteasy.spi.ConstructorInjector;
import org.jboss.resteasy.spi.InjectorFactory;
import org.jboss.resteasy.spi.MethodInjector;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.spi.ValueInjector;
import org.jboss.resteasy.spi.metadata.Parameter;
import org.jboss.resteasy.spi.metadata.ResourceClass;
import org.jboss.resteasy.spi.metadata.ResourceConstructor;
import org.jboss.resteasy.spi.metadata.ResourceLocator;

/**
 * An {@linkplain InjectorFactory injector factory} which uses CDI to lookup beans for injected values. If no CDI bean
 * was found, standard Jakarta REST injection is used.
 *
 * @author Jozef Hartinger
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @see InjectorFactoryImpl
 */
public class CdiInjectorFactory implements InjectorFactory {
    private final BeanManager manager;
    private final InjectorFactory delegate = new InjectorFactoryImpl();
    private final Map<Class<?>, Type> sessionBeanInterface;

    /**
     * Creates a new injector factory which uses CDI for the injected values.
     *
     * @param manager the CDI bean manager
     */
    public CdiInjectorFactory(final BeanManager manager) {
        this.manager = manager;
        sessionBeanInterface = resolveSessionInterfaceBeans(manager);
    }

    @Override
    public ValueInjector createParameterExtractor(Parameter parameter, ResteasyProviderFactory providerFactory) {
        return delegate.createParameterExtractor(parameter, providerFactory);
    }

    @Override
    public MethodInjector createMethodInjector(ResourceLocator method, ResteasyProviderFactory factory) {
        return delegate.createMethodInjector(method, factory);
    }

    @Override
    public PropertyInjector createPropertyInjector(ResourceClass resourceClass, ResteasyProviderFactory providerFactory) {
        return new CdiPropertyInjector(delegate.createPropertyInjector(resourceClass, providerFactory),
                resourceClass.getClazz(), sessionBeanInterface, manager);
    }

    @Override
    public ConstructorInjector createConstructor(ResourceConstructor constructor, ResteasyProviderFactory providerFactory) {
        Class<?> clazz = constructor.getConstructor().getDeclaringClass();

        ConstructorInjector injector = cdiConstructor(clazz);
        if (injector != null)
            return injector;

        LogMessages.LOGGER.debugf("No CDI bean found for %s. Using default constructor %s.", clazz, constructor);
        return delegate.createConstructor(constructor, providerFactory);
    }

    @Override
    public ConstructorInjector createConstructor(Constructor constructor, ResteasyProviderFactory factory) {
        Class<?> clazz = constructor.getDeclaringClass();

        ConstructorInjector injector = cdiConstructor(clazz);
        if (injector != null)
            return injector;

        LogMessages.LOGGER.debugf("No CDI bean found for %s. Using default constructor %s.", clazz, constructor);
        return delegate.createConstructor(constructor, factory);
    }

    @Override
    public PropertyInjector createPropertyInjector(Class resourceClass, ResteasyProviderFactory factory) {
        return new CdiPropertyInjector(delegate.createPropertyInjector(resourceClass, factory), resourceClass,
                sessionBeanInterface, manager);
    }

    @Override
    public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget, String defaultName,
            Class type, Type genericType, Annotation[] annotations, ResteasyProviderFactory factory) {
        return delegate.createParameterExtractor(injectTargetClass, injectTarget, defaultName, type, genericType, annotations,
                factory);
    }

    @Override
    public ValueInjector createParameterExtractor(Class injectTargetClass, AccessibleObject injectTarget, String defaultName,
            Class type,
            Type genericType, Annotation[] annotations, boolean useDefault, ResteasyProviderFactory factory) {
        return delegate.createParameterExtractor(injectTargetClass, injectTarget, defaultName, type, genericType, annotations,
                useDefault, factory);
    }

    private ConstructorInjector cdiConstructor(Class<?> clazz) {
        if (!manager.getBeans(clazz).isEmpty()) {
            return new CdiConstructorInjector(clazz, manager);
        }

        if (sessionBeanInterface.containsKey(clazz)) {
            Type intfc = sessionBeanInterface.get(clazz);
            LogMessages.LOGGER.debugf("Using %s for lookup of Session Bean %s.", intfc, clazz);
            return new CdiConstructorInjector(intfc, manager);
        }

        return null;
    }

    private static Map<Class<?>, Type> resolveSessionInterfaceBeans(final BeanManager manager) {
        Set<Bean<?>> beans = manager.getBeans(RestClientExtension.class);
        Bean<?> bean = manager.resolve(beans);
        if (bean == null) {
            LogMessages.LOGGER.extensionLookupFailed(RestClientExtension.class.getName());
            return Map.of();
        }
        CreationalContext<?> context = manager.createCreationalContext(bean);
        return ((RestClientExtension) manager.getReference(bean, RestClientExtension.class, context)).getSessionBeanInterface();
    }
}
