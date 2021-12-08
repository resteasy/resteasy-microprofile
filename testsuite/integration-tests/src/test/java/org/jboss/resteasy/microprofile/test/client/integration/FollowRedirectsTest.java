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

import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.FollowRedirectsResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.FollowRedirectsService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.FollowRedirectsServiceIntf;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @tpSubChapter MicroProfile rest client
 * @tpChapter Integration tests
 * @tpTestCaseDetails Show followsRedirects flag works.
 * @tpSince RESTEasy 4.6.0
 */
@RunWith(Arquillian.class)
@RunAsClient
public class FollowRedirectsTest {
    private static final String WAR_SERVICE = "followRedirects_service";
    private static final String WAR_CLIENT = "followRedirects_client";
    private static final String THE_PATRON = "thePatron";

    @Deployment(name = WAR_SERVICE)
    public static Archive<?> serviceDeploy() {
        return TestEnvironment.createWar(WAR_SERVICE)
                .addClasses(FollowRedirectsService.class);
    }

    @Deployment(name = WAR_CLIENT)
    public static Archive<?> clientDeploy() {
        return TestEnvironment.createWar(WAR_CLIENT)
                .addClasses(FollowRedirectsResource.class, FollowRedirectsServiceIntf.class);
    }

    private FollowRedirectsServiceIntf followRedirectsServiceIntf;
    @ArquillianResource
    @OperateOnDeployment(WAR_SERVICE)
    private URL url;

    @Before
    public void before() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        followRedirectsServiceIntf = builder
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .followRedirects(true)
                .build(FollowRedirectsServiceIntf.class);
    }

    /*
     * Default setting for followRedirects is FALSE.
     * Confirm no redirection.
     */

    @Test
    public void defaultFollowRedirects() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        FollowRedirectsServiceIntf result = builder
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .build(FollowRedirectsServiceIntf.class);

        Response response = result.tmpRedirect(THE_PATRON, WAR_CLIENT);
        Assert.assertEquals(307, response.getStatus());
        response.close();
    }

    /*
     * Set followRedirects ON and confirm it is working.
     */
    @Test
    public void followTemporaryRedirect() {
        Response response = followRedirectsServiceIntf.tmpRedirect(THE_PATRON, WAR_CLIENT);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("OK", response.readEntity(String.class));
        response.close();
    }

    /*
     * Confirm 303 status redirect with POST works.
     */
    @Test
    public void postRedirect() {
        Response response = followRedirectsServiceIntf.postRedirect(WAR_CLIENT);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("OK", response.readEntity(String.class));
        response.close();
    }

    /*
     * Confirm 301 status with "location" header.
     */
    @Test
    public void movedPermanently() {
        Response response = followRedirectsServiceIntf.movedPermanently(THE_PATRON, WAR_CLIENT);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("ok - direct response",
                response.readEntity(String.class));
        response.close();
    }

    /*
     * Confirm 302 status with "location" header.
     */
    @Test
    public void found() {
        Response response = followRedirectsServiceIntf.found(THE_PATRON, WAR_CLIENT);
        Assert.assertEquals(200, response.getStatus());
        Assert.assertEquals("ok - direct response",
                response.readEntity(String.class));
        response.close();
    }
}
