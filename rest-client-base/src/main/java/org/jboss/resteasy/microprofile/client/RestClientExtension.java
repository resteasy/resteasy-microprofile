/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2021 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.resteasy.microprofile.client;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.ProcessSessionBean;
import jakarta.enterprise.inject.spi.WithAnnotations;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

public class RestClientExtension implements Extension {

    private final Map<Type, RestClientData<?>> proxyTypes = new LinkedHashMap<>();

    private final Set<Throwable> errors = new LinkedHashSet<>();

    private final Map<Class<?>, Type> sessionBeanInterface = new HashMap<>();

    public void registerRestClient(@Observes @WithAnnotations(RegisterRestClient.class) ProcessAnnotatedType<?> type) {
        Class<?> javaClass = type.getAnnotatedType().getJavaClass();
        if (javaClass.isInterface()) {
            RegisterRestClient annotation = type.getAnnotatedType().getAnnotation(RegisterRestClient.class);
            Optional<String> maybeUri = extractBaseUri(annotation);
            Optional<String> maybeConfigKey = extractConfigKey(annotation);

            proxyTypes.put(javaClass, new RestClientData<>(javaClass, maybeUri, maybeConfigKey));
            // no need to veto() these types because interfaces cannot become beans anyway
        } else {
            errors.add(new IllegalArgumentException("Rest client needs to be an interface " + javaClass));
        }
    }

    public void injectionPoint(@Observes final ProcessInjectionPoint<?, ?> injectionPoint) {
        if (injectionPoint.getInjectionPoint().getAnnotated().isAnnotationPresent(RestClient.class)) {
            final Type beanType = injectionPoint.getInjectionPoint().getType();
            final RestClientData<?> data = proxyTypes.get(beanType);
            if (data != null) {
                // Use the class loader from the declaring type to be used to load the MicroProfile Config
                data.classLoader = injectionPoint.getInjectionPoint().getMember().getDeclaringClass().getClassLoader();
            }
        }
    }

    private Optional<String> extractBaseUri(RegisterRestClient annotation) {
        String baseUri = annotation.baseUri();
        return Optional.ofNullable("".equals(baseUri) ? null : baseUri);
    }

    private Optional<String> extractConfigKey(RegisterRestClient annotation) {
        String configKey = annotation.configKey();
        return Optional.ofNullable("".equals(configKey) ? null : configKey);
    }

    public void createProxy(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        for (RestClientData<?> clientData : proxyTypes.values()) {
            afterBeanDiscovery.addBean(
                    new RestClientDelegateBean<>(clientData.javaClass,
                            // If the class loader is null, use the class loader from the client
                            (clientData.classLoader == null ? clientData.javaClass.getClassLoader() : clientData.classLoader),
                            beanManager, clientData.baseUri, clientData.configKey));
        }
        // Clear the proxy types as we no longer need them
        proxyTypes.clear();
    }

    public void reportErrors(@Observes AfterDeploymentValidation afterDeploymentValidation) {
        for (Throwable error : errors) {
            afterDeploymentValidation.addDeploymentProblem(error);
        }
    }

    /**
     * Observes ProcessSessionBean events and creates a (Bean class {@literal ->} Local
     * interface) map for Session beans with local interfaces. This map is
     * necessary since RESTEasy identifies a bean class as JAX-RS components
     * while CDI requires a local interface to be used for lookup.
     *
     * @param event event the process session bean event
     */
    public <T> void observeSessionBeans(@Observes ProcessSessionBean<T> event) {
        final Bean<Object> sessionBean = event.getBean();

        if (CdiHelper.isRestComponent(sessionBean.getBeanClass())) {
            addSessionBeanInterface(sessionBean);
        }
    }

    private void addSessionBeanInterface(Bean<?> bean) {
        for (Type type : bean.getTypes()) {
            if ((type instanceof Class<?>) && ((Class<?>) type).isInterface()) {
                Class<?> clazz = (Class<?>) type;
                if (CdiHelper.isRestAnnotatedClass(clazz)) {
                    sessionBeanInterface.put(bean.getBeanClass(), type);
                    return;
                }
            }
        }
    }

    public Map<Class<?>, Type> getSessionBeanInterface() {
        return Map.copyOf(sessionBeanInterface);
    }

    /**
     * @return {@code true} if CDI is believed to be activated, otherwise {@code false}
     *
     * @deprecated this method is not supported and will eventually be deleted
     */
    @Deprecated
    public static boolean isCDIActive() {
        try {
            return CDI.current().getBeanManager() != null;
        } catch (IllegalStateException ise) {
            // This happens when a CDIProvider is not available.
            return false;
        }
    }

    /**
     * This method currently does nothing.
     *
     * @deprecated this method is not supported and will eventually be deleted
     */
    @Deprecated
    public static void clearBeanManager() {
        // nothing to do
    }

    private static class RestClientData<T> {
        private final Class<T> javaClass;
        private final Optional<String> baseUri;
        private final Optional<String> configKey;
        private volatile ClassLoader classLoader;

        private RestClientData(final Class<T> javaClass, final Optional<String> baseUri,
                final Optional<String> configKey) {
            this.javaClass = javaClass;
            this.baseUri = baseUri;
            this.configKey = configKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            RestClientData<?> that = (RestClientData<?>) o;
            return javaClass.equals(that.javaClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(javaClass);
        }
    }

    /**
     * Lifted from CdiConstructorInjector in resteasy-cdi
     *
     * @deprecated this method is not supported and will eventually be deleted
     */
    @Deprecated
    public static Object construct(Class<?> clazz) {
        BeanManager manager;
        try {
            manager = CDI.current().getBeanManager();
        } catch (IllegalStateException ignore) {
            return null;
        }
        if (manager != null) {
            Set<Bean<?>> beans = manager.getBeans(clazz);
            if (beans.isEmpty()) {
                return null;
            }

            if (beans.size() > 1) {
                Set<Bean<?>> modifiableBeans = new HashSet<>();
                modifiableBeans.addAll(beans);
                // Ambiguous dependency may occur if a resource has subclasses
                // Therefore we remove those beans
                for (Iterator<Bean<?>> iterator = modifiableBeans.iterator(); iterator.hasNext();) {
                    Bean<?> bean = iterator.next();
                    if (!bean.getBeanClass().equals(clazz) && !bean.isAlternative()) {
                        // remove Beans that have clazz in their type closure but not as a base class
                        iterator.remove();
                    }
                }
                beans = modifiableBeans;
            }
            Bean<?> bean = manager.resolve(beans);
            if (bean == null) {
                return null;
            }
            CreationalContext<?> context = manager.createCreationalContext(bean);
            if (context == null) {
                return null;
            }
            return manager.getReference(bean, clazz, context);
        } else {
            // CDI is not active.
            return null;
        }
    }
}
