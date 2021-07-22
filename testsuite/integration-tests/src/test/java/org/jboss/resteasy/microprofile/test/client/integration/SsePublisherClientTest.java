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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.client.BuilderResolver;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPSseClient;
import org.jboss.resteasy.microprofile.test.client.integration.resource.MPSseResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@RunWith(Arquillian.class)
@RunAsClient
public class SsePublisherClientTest {
    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> deploy() {
        return TestEnvironment.createWar(SsePublisherClientTest.class)
                .addClasses(MPSseResource.class);
    }

    @Test
    public void testSseClient() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        RestClientBuilder resteasyBuilder = new BuilderResolver().newBuilder();
        assertEquals(resteasyBuilder.getClass(), builder.getClass());
        MPSseClient client = builder.baseUri(TestEnvironment.generateUri(url, "test-app")).build(MPSseClient.class);
        Publisher<String> publisher = client.getStrings();
        CountDownLatch resultsLatch = new CountDownLatch(5);

        final Set<String> eventStrings = new HashSet<>();
        StringSubscriber subscriber = new StringSubscriber(eventStrings, resultsLatch);
        publisher.subscribe(subscriber);
        Thread.sleep(1000);
        subscriber.request(5);
        assertTrue(resultsLatch.await(10, TimeUnit.SECONDS));
        //sent 12 items, expects these 10 values [msg4, msg3, msg2, msg1, msg8, msg11, msg7, msg10, msg9, msg0]
        assertTrue(eventStrings.size() == 10);
        //msg5 and msg6 are dropped
        // TODO (jrp) determine the validity of this
        // assertFalse("Expected [msg4, msg3, msg2, msg1, msg8, msg11, msg7, msg10, msg9, msg0], found "+  eventStrings,
        //         eventStrings.contains("msg5") || eventStrings.contains("msg6"));
        assertNull(subscriber.throwable);
    }

    private static class StringSubscriber implements Subscriber<String>, AutoCloseable {

        final CountDownLatch eventLatch;
        Throwable throwable;
        Subscription subscription;
        Set<String> eventStrings;

        StringSubscriber(final Set<String> eventStrings, final CountDownLatch eventLatch) {
            this.eventLatch = eventLatch;
            this.eventStrings = eventStrings;
        }

        @Override
        public void onSubscribe(Subscription s) {
            subscription = s;
            request(5);
        }

        @Override
        public void onNext(String s) {
            eventStrings.add(s);
            eventLatch.countDown();
        }

        @Override
        public void onError(Throwable t) {
            throwable = t;
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void close() throws Exception {
            subscription.cancel();
        }

        public void request(long requestedEvents) {
            subscription.request(requestedEvents);
        }
    }
}
