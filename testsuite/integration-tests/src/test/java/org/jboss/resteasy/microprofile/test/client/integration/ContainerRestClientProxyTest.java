/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
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

package org.jboss.resteasy.microprofile.test.client.integration;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ReflectPermission;
import java.util.PropertyPermission;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Intercepted;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
public class ContainerRestClientProxyTest {

    @Deployment
    public static WebArchive deployment() throws IOException {
        return TestEnvironment.createWarWithConfigUrl(ContainerRestClientProxyTest.class, InterceptedClient.class, "test-app")
                .addClasses(
                        InterceptedClient.class,
                        TestResource.class,
                        ClientInterceptor.class,
                        ClientInterceptorBinding.class,
                        ClientMethodInterceptor.class,
                        ClientMethodInterceptorBinding.class)
                .addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(
                        new PropertyPermission("arquillian.*", "read"),
                        new ReflectPermission("suppressAccessChecks"),
                        new RuntimePermission("accessDeclaredMembers")),
                        "permissions.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private InterceptedClient client;

    @Inject
    @RestClient
    private InterceptedClient qualifiedClient;

    @Test
    public void checkQualifiedClient() {
        Assertions.assertNotNull(qualifiedClient, "Cliented injected with qualifier @RestClient should not be null");
    }

    @Test
    public void intercepted() {
        Assertions.assertNotNull(client);
        Assertions.assertFalse(ClientInterceptor.invoked);
        Assertions.assertFalse(ClientMethodInterceptor.invoked);
        Assertions.assertEquals("test", client.get());
        Assertions.assertTrue(ClientInterceptor.invoked);
        Assertions.assertTrue(ClientMethodInterceptor.invoked);
    }

    @RegisterRestClient
    @RequestScoped
    @ClientInterceptorBinding
    @Path("/test")
    public interface InterceptedClient {

        @GET
        @Path("/test")
        @ClientMethodInterceptorBinding
        String get();

    }

    @Path("/test")
    public static class TestResource {
        @GET
        @Path("/test")
        public String get() {
            return "test";
        }
    }

    @ClientInterceptorBinding
    @Priority(1)
    @Interceptor
    public static class ClientInterceptor {

        public static boolean invoked = false;

        @Inject
        @Intercepted
        Bean<?> bean;

        @AroundInvoke
        public Object doSomething(InvocationContext ic) throws Exception {
            invoked = true;
            return ic.proceed();
        }
    }

    @Priority(1)
    @Interceptor
    @ClientMethodInterceptorBinding
    public static class ClientMethodInterceptor {

        public static boolean invoked = false;

        @Inject
        @Intercepted
        Bean<?> bean;

        @AroundInvoke
        public Object doSomething(InvocationContext ic) throws Exception {
            invoked = true;
            return ic.proceed();
        }
    }

    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
    public @interface ClientInterceptorBinding {
    }

    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
    public @interface ClientMethodInterceptorBinding {
    }
}
