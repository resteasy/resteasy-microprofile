/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
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

package org.jboss.resteasy.microprofile.test.client.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;

import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ClientRootResource;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ClientSubResource;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ServerResource;
import org.jboss.resteasy.microprofile.test.client.exception.resource.TestException;
import org.jboss.resteasy.microprofile.test.client.exception.resource.TestExceptionMapper;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests client sub-resources
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SubResourceTest {

    @ArquillianResource
    private URL url;

    @Deployment
    public static WebArchive deployment() {
        return TestEnvironment.createWar(SubResourceTest.class)
                .addClasses(ServerResource.class);
    }

    /**
     * Creates a REST client with an attached exception mapper. The exception mapper will throw a {@link TestException}.
     * This test invokes a call to the root resource.
     *
     * @throws Exception if a test error occurs
     */
    @Test
    public void rootResourceExceptionMapper() throws Exception {
        try (ClientRootResource root = createClient(TestExceptionMapper.class)) {
            try (Response ignore = root.fromRoot()) {
                Assert.fail("fromRoot() should have thrown an TestException");
            } catch (TestException expected) {
                Assert.assertEquals("RootResource failed on purpose", expected.getMessage());
            } catch (Exception e) {
                failWithException(e, "fromRoot");
            }
        }
    }

    /**
     * Creates a REST client with an attached exception mapper. The exception mapper will throw a {@link TestException}.
     * This test invokes a call to the sub-resource. The sub-resource then invokes an additional call which should also
     * result in a {@link TestException} thrown.
     *
     * @throws Exception if a test error occurs
     */
    @Test
    public void subResourceExceptionMapper() throws Exception {
        try (ClientRootResource root = createClient(TestExceptionMapper.class)) {
            final ClientSubResource subResource = root.subResource();
            Assert.assertNotNull("The SubResource should not be null", subResource);
            try (Response ignore = subResource.fromSub()) {
                Assert.fail("fromSub() should have thrown an TestException");
            } catch (TestException expected) {
                Assert.assertEquals("SubResource failed on purpose", expected.getMessage());
            } catch (Exception e) {
                failWithException(e, "fromSub");
            }
        }
    }

    /**
     * This test invokes a call to the sub-resource. The sub-resource then invokes an additional call which should
     * return the header value for {@code test-header}.
     *
     * @throws Exception if a test error occurs
     */
    @Test
    public void subResourceWithHeader() throws Exception {
        try (ClientRootResource root = createClient()) {
            final ClientSubResource subResource = root.subResource();
            Assert.assertNotNull("The SubResource should not be null", subResource);
            try (Response response = subResource.withHeader()) {
                Assert.assertEquals(Response.Status.OK, response.getStatusInfo());
                final String value = response.readEntity(String.class);
                Assert.assertEquals("SubResourceHeader", value);
            }
        }
    }

    /**
     * This test invokes a call to the sub-resource. The sub-resource then invokes an additional call which should
     * return the header value for {@code test-global-header}.
     *
     * @throws Exception if a test error occurs
     */
    @Test
    public void subResourceWithGlobalHeader() throws Exception {
        try (ClientRootResource root = createClient()) {
            final ClientSubResource subResource = root.subResource();
            Assert.assertNotNull("The SubResource should not be null", subResource);
            try (Response response = subResource.withGlobalHeader()) {
                Assert.assertEquals(Response.Status.OK, response.getStatusInfo());
                final String value = response.readEntity(String.class);
                Assert.assertEquals("GlobalSubResourceHeader", value);
            }
        }
    }

    private ClientRootResource createClient() throws URISyntaxException {
        return createClient(null);
    }

    private ClientRootResource createClient(final Class<?> componentType) throws URISyntaxException {
        final RestClientBuilder builder = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "test-app"));
        if (componentType != null) {
            builder.register(componentType);
        }
        return builder.build(ClientRootResource.class);
    }

    private static void failWithException(final Exception e, final String methodName) {
        final StringWriter writer = new StringWriter();
        writer.write(methodName);
        writer.write("() should have thrown an TestException. Instead got: ");
        writer.write(System.lineSeparator());
        e.printStackTrace(new PrintWriter(writer));
        Assert.fail(writer.toString());
    }
}
