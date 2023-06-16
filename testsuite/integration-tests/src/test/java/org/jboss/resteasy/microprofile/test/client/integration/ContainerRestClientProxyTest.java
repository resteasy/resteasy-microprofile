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
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.resteasy.utils.PermissionUtil;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
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
                .addAsManifestResource(PermissionUtil.createPermissionsXmlAsset(
                        new PropertyPermission("arquillian.*", "read"),
                        new ReflectPermission("suppressAccessChecks"),
                        new RuntimePermission("accessDeclaredMembers")),
                        "permissions.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    @RestClient
    private InterceptedClient client;

    @Test
    public void intercepted() {
        Assert.assertNotNull(client);
        Assert.assertFalse(ClientInterceptor.invoked);
        Assert.assertFalse(ClientMethodInterceptor.invoked);
        Assert.assertEquals("test", client.get());
        Assert.assertTrue(ClientInterceptor.invoked);
        Assert.assertTrue(ClientMethodInterceptor.invoked);
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
