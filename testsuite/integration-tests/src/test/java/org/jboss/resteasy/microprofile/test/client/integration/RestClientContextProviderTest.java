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

import java.net.URL;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.TestAsyncClient;
import org.jboss.resteasy.microprofile.test.client.integration.resource.TestAsyncFilter;
import org.jboss.resteasy.microprofile.test.client.integration.resource.TestAsyncInvocationInterceptorFactory;
import org.jboss.resteasy.microprofile.test.client.integration.resource.TestAsyncResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class RestClientContextProviderTest {

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> deploy() {
        return TestEnvironment.createWar(RestClientContextProviderTest.class)
                .addClasses(TestAsyncResource.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void asyncInvocationInterceptors() throws Exception {
        final String currentThreadName = Thread.currentThread().getName();
        final String asyncThreadName = "Test-Async-Thread";

        final ThreadFactory testThreadFactory = r -> {
            final Thread result = new Thread(r);
            result.setName(asyncThreadName);
            result.setDaemon(true);
            return result;
        };

        final ExecutorService testExecutorService = Executors.newSingleThreadExecutor(testThreadFactory);

        final CountDownLatch removed = new CountDownLatch(1);
        final TestAsyncInvocationInterceptorFactory factory = new TestAsyncInvocationInterceptorFactory(removed);
        final TestAsyncClient client = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .register(TestAsyncFilter.class)
                .register(factory)
                .executorService(testExecutorService)
                .build(TestAsyncClient.class);
        final Response response = client.get().toCompletableFuture().get(5, TimeUnit.SECONDS);
        Assert.assertEquals(Response.Status.OK, response.getStatusInfo());
        Assert.assertEquals(String.format("{prepareContext=%s, applyContext=%s}", currentThreadName, asyncThreadName),
                response.getHeaderString("test1-state"));

        // Wait until the context has been removed
        Assert.assertTrue("Timeout waiting for remove to be invoked.", removed.await(5, TimeUnit.SECONDS));
        final Map<String, String> data = TestAsyncInvocationInterceptorFactory.localState.get();
        Assert.assertEquals("Expected 3 entries found: " + data, 3, data.size());
        Assert.assertEquals(currentThreadName, data.get("prepareContext"));
        Assert.assertEquals(asyncThreadName, data.get("applyContext"));
        Assert.assertEquals(asyncThreadName, data.get("removeContext"));

        testExecutorService.shutdownNow();
    }
}
