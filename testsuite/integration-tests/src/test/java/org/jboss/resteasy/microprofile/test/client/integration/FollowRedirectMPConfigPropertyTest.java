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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.client.RestClientBuilderImpl;
import org.jboss.resteasy.microprofile.test.client.integration.resource.FollowRedirectsService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.FollowRedirectsServiceIntf;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @tpSubChapter MicroProfile rest client
 * @tpChapter Integration tests
 * @tpTestCaseDetails Show using microprofile-config property, "/mp-rest/followRedirects" works.
 * @tpSince RESTEasy 4.6.0
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class FollowRedirectMPConfigPropertyTest {
    private static final String MP_REST_FOLLOWREDIRECT = "/mp-rest/followRedirects";

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> serviceDeploy() {
        return TestEnvironment.createWar(FollowRedirectMPConfigPropertyTest.class)
                .addClass(FollowRedirectsService.class);
    }

    private URI generateURL() throws URISyntaxException {
        return TestEnvironment.generateUri(url, "test-app");
    }

    @Test
    public void fullyQualifiedName() throws Exception {
        String key = FollowRedirectsServiceIntf.class.getCanonicalName() + MP_REST_FOLLOWREDIRECT;
        System.setProperty(key, "true");

        RestClientBuilder builder = RestClientBuilder.newBuilder();
        FollowRedirectsServiceIntf followRedirectsServiceIntf = builder
                .baseUri(generateURL())
                .build(FollowRedirectsServiceIntf.class);
        testRedirected(followRedirectsServiceIntf, Response.Status.OK, "pong");
        System.clearProperty(key);
    }

    @Test
    public void simpleName() throws Exception {
        String key = "ckName" + MP_REST_FOLLOWREDIRECT;
        System.setProperty(key, "true");

        RestClientBuilder builder = RestClientBuilder.newBuilder();
        FollowRedirectsServiceIntf followRedirectsServiceIntf = builder
                .baseUri(generateURL())
                .build(FollowRedirectsServiceIntf.class);
        testRedirected(followRedirectsServiceIntf, Response.Status.OK, "pong");
        System.clearProperty(key);
    }

    @Test
    public void badValue() throws Exception {
        String key = "ckName" + MP_REST_FOLLOWREDIRECT;
        System.setProperty(key, "maybe");

        RestClientBuilderImpl builder = (RestClientBuilderImpl) RestClientBuilder.newBuilder();
        FollowRedirectsServiceIntf followRedirectsServiceIntf = builder
                .baseUri(generateURL())
                .build(FollowRedirectsServiceIntf.class);
        testRedirected(followRedirectsServiceIntf, Response.Status.TEMPORARY_REDIRECT, "");
        System.clearProperty(key);
    }

    private void testRedirected(final FollowRedirectsServiceIntf followRedirectsServiceIntf,
            final Response.Status expectedStatus,
            final String expectedText) {
        try (Response response = followRedirectsServiceIntf.redirectPing()) {
            Assertions.assertEquals(expectedStatus.getStatusCode(), response.getStatus());
            Assertions.assertEquals(expectedText, response.readEntity(String.class));
        }
    }
}
