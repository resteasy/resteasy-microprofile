/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2022 Red Hat, Inc., and individual contributors
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

package org.jboss.resteasy.microprofile.test.client.mapping;

import java.net.URL;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class FeatureContextMapperTest {

    @Deployment
    public static WebArchive deployment() {
        return TestEnvironment.createWar(FeatureContextMapperTest.class)
                .addClasses(FeatureContextMapperTest.class,
                        TestResource.class,
                        TestClient.class,
                        TestFeature.class,
                        TestException.class,
                        TestResponseExceptionMapper.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @ArquillianResource
    URL url;

    @Test
    public void mapperRegistered() throws Exception {
        TestClient client = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .build(TestClient.class);
        Assert.assertNotNull(client);
        try {
            final String value = client.test();
            Assert.fail(String.format("Expected the client to throw a TestException but got a value of %s", value));
        } catch (TestException expected) {
            final String msg = expected.getLocalizedMessage();
            final Response response = expected.getResponse();
            Assert.assertEquals("test", msg);
            Assert.assertEquals(Response.Status.NOT_IMPLEMENTED, response.getStatusInfo());
        }
    }

    @RegisterRestClient
    @RegisterProvider(TestFeature.class)
    public interface TestClient {

        @GET
        @Path("/test")
        String test() throws TestException;
    }

    @Path("/test")
    @RequestScoped
    public static class TestResource {
        @GET
        public String test() throws TestException {
            throw new TestException("resource", Response.status(Response.Status.NOT_IMPLEMENTED).build());
        }
    }

    public static class TestFeature implements Feature {

        @Override
        public boolean configure(final FeatureContext context) {
            context.register(new TestResponseExceptionMapper());
            return true;
        }
    }

    private static class TestException extends WebApplicationException {
        TestException(final String msg, final Response response) {
            super(msg, response);
        }
    }

    private static class TestResponseExceptionMapper implements ResponseExceptionMapper<TestException> {

        @Override
        public TestException toThrowable(final Response response) {
            return new TestException("test", response);
        }

        @Override
        public boolean handles(final int status, final MultivaluedMap<String, Object> headers) {
            return true;
        }

        @Override
        public int getPriority() {
            return 100;
        }
    }
}
