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

package org.jboss.resteasy.microprofile.test.client.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.engines.vertx.VertxClientHttpEngine;
import org.jboss.resteasy.microprofile.client.BuilderResolver;
import org.jboss.resteasy.microprofile.test.client.integration.resource.HeaderPropagator;
import org.jboss.resteasy.microprofile.test.client.integration.resource.HelloClient;
import org.jboss.resteasy.microprofile.test.client.integration.resource.HelloResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.NgHTTP2;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;

@RunWith(Arquillian.class)
@RunAsClient
public class RestClientProxyTest {

    public static final String EMOJIS = "\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00\uD83D\uDE00";
    @ArquillianResource
    URL url;

    @Deployment
    public static Archive<?> deploy() throws IOException {
        return TestEnvironment.createWarWithConfigUrl(RestClientProxyTest.class, HelloClient.class, "test-app")
                .addClasses(
                        HelloClient.class,
                        HelloResource.class,
                        HeaderPropagator.class,
                        TestParamConverter.class,
                        TestParamConverterProvider.class,
                        NgHTTP2.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    private URI generateUri() throws URISyntaxException {
        return TestEnvironment.generateUri(url, "test-app");
    }

    @Test
    public void testHTTP2ByRegistration() {
        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri(URI.create("https://nghttp2.org/"));

        Vertx vertx = Vertx.vertx();

        HttpClientOptions options = new HttpClientOptions();
        options.setSsl(true);
        options.setProtocolVersion(HttpVersion.HTTP_2);
        options.setUseAlpn(true);

        builder.register(new VertxClientHttpEngine(vertx, options));

        NgHTTP2 client = builder.build(NgHTTP2.class);

        final String resp = client.get();
        assertTrue(resp.contains("nghttp2.org"));
    }

    @Test
    public void testGetClient() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        RestClientBuilder resteasyBuilder = new BuilderResolver().newBuilder();
        assertEquals(resteasyBuilder.getClass(), builder.getClass());
        HelloClient client = builder.baseUri(generateUri()).build(HelloClient.class);

        assertNotNull(client);
        assertEquals("Hello", client.hello());
    }

    @Test
    public void testGetSingle() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        HelloClient client = builder.baseUri(generateUri()).build(HelloClient.class);

        assertNotNull(client);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> value = new AtomicReference<>();
        value.set(null);
        CompletionStage<String> cs = client.some("foo");
        cs.thenApply((s) -> {
            value.set(s);
            latch.countDown();
            return s;
        });
        boolean waitResult = latch.await(30, TimeUnit.SECONDS);
        Assert.assertTrue("Waiting for event to be delivered has timed out.", waitResult);
        assertEquals("foo", value.get());
    }

    //RESTEASY-2633
    @Test
    public void testEncodingMultiByteCharacters() throws Exception {
        final RestClientBuilder builder = RestClientBuilder.newBuilder();
        final HelloClient client = builder.baseUri(generateUri()).build(HelloClient.class);
        final CompletionStage<String> cs = client.some(EMOJIS);
        final CompletableFuture<String> future = cs.toCompletableFuture();
        assertEquals(EMOJIS, future.get(30, TimeUnit.SECONDS));
    }

    @Test
    public void testGetCompletionStage() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        HelloClient client = builder.baseUri(generateUri()).build(HelloClient.class);

        assertNotNull(client);
        final CompletionStage<String> cs = client.some("foo");
        final CompletableFuture<String> future = cs.toCompletableFuture();
        assertEquals("foo", future.get(30, TimeUnit.SECONDS));
    }

    @Test
    public void testHeadersPropagation() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        HelloClient client = builder.baseUri(generateUri()).build(HelloClient.class);

        assertNotNull(client);
        CompletionStage<String> cs = client.asyncClient();
        assertEquals("OK", cs.toCompletableFuture().get(30, TimeUnit.SECONDS));

        assertEquals("OK", client.client());
    }

    @Test
    public void testAsyncClient404() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        HelloClient client = builder.baseUri(generateUri()).build(HelloClient.class);

        assertNotNull(client);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> value = new AtomicReference<>();
        value.set(null);
        CompletionStage<String> cs = client.asyncClient404();
        cs.whenComplete((String s, Throwable t) -> {
            value.set(t.getCause());
            latch.countDown();
        });
        boolean waitResult = latch.await(30, TimeUnit.SECONDS);
        Assert.assertTrue("Waiting for event to be delivered has timed out.", waitResult);
        assertTrue(value.get() instanceof WebApplicationException);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ((WebApplicationException) value.get()).getResponse()
                .getStatus());
    }

    @Test
    public void testNullPathParam() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        RestClientBuilder resteasyBuilder = new BuilderResolver().newBuilder();
        assertEquals(resteasyBuilder.getClass(), builder.getClass());
        HelloClient client = builder
                .baseUri(generateUri())
                .build(HelloClient.class);

        assertNotNull(client);
        assertEquals("testPath", client.nullPathParam("testPath"));
        try {
            client.nullPathParam(null);
            fail("A null path parameter should not be allowed");
        } catch (NullPointerException e) {
            // Given the generic error we want to ensure we're getting an NPE from the correct spot. We'll validate this
            // via the message id.
            final String msg = e.getLocalizedMessage();
            final String msgId = "RESTEASY004690";
            assertTrue(String.format("Expected message to start with %s: %s", msgId, msg), msg.startsWith(msgId));
        }
    }

    @Test
    public void testNullPathParamWithConverter() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        RestClientBuilder resteasyBuilder = new BuilderResolver().newBuilder();
        assertEquals(resteasyBuilder.getClass(), builder.getClass());
        HelloClient client = builder
                .baseUri(generateUri())
                .register(TestParamConverterProvider.class)
                .build(HelloClient.class);

        assertNotNull(client);
        assertEquals("testPath", client.nullPathParam("testPath"));
        try {
            client.nullPathParam(null);
            fail("A null path parameter should not be allowed");
        } catch (NullPointerException e) {
            // Given the generic error we want to ensure we're getting an NPE from the correct spot. We'll validate this
            // via the message id.
            final String msg = e.getLocalizedMessage();
            final String msgId = "RESTEASY004690";
            assertTrue(String.format("Expected message to start with %s: %s", msgId, msg), msg.startsWith(msgId));
        }
    }

    @Test
    public void testNullQueryParam() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        RestClientBuilder resteasyBuilder = new BuilderResolver().newBuilder();
        assertEquals(resteasyBuilder.getClass(), builder.getClass());
        HelloClient client = builder
                .baseUri(generateUri())
                .build(HelloClient.class);

        assertNotNull(client);
        assertEquals("testPath", client.nullQueryParam("testPath"));
        assertNull(client.nullQueryParam(null));
    }

    @Test
    public void testNullQueryParamWithConverter() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        RestClientBuilder resteasyBuilder = new BuilderResolver().newBuilder();
        assertEquals(resteasyBuilder.getClass(), builder.getClass());
        HelloClient client = builder
                .baseUri(generateUri())
                .register(TestParamConverterProvider.class)
                .build(HelloClient.class);

        assertNotNull(client);
        assertEquals("testPath", client.nullQueryParam("testPath"));
        assertNull(client.nullQueryParam(null));
    }

    public static class TestParamConverter implements ParamConverter<String> {

        @Override
        public String fromString(final String value) {
            return value;
        }

        @Override
        public String toString(final String value) {
            return value;
        }
    }

    @Provider
    public static class TestParamConverterProvider implements ParamConverterProvider {
        private final TestParamConverter converter = new TestParamConverter();

        @Override
        @SuppressWarnings("unchecked")
        public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType,
                final Annotation[] annotations) {
            if (Objects.equals(rawType, CharSequence.class)) {
                return (ParamConverter<T>) converter;
            }
            return null;
        }
    }
}
