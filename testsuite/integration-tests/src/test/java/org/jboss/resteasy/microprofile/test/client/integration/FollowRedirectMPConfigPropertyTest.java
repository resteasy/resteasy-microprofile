package org.jboss.resteasy.microprofile.test.client.integration;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.client.RestClientBuilderImpl;
import org.jboss.resteasy.microprofile.test.client.integration.resource.FollowRedirectsService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.FollowRedirectsServiceIntf;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @tpSubChapter MicroProfile rest client
 * @tpChapter Integration tests
 * @tpTestCaseDetails Show using microprofile-config property, "/mp-rest/followRedirects" works.
 * @tpSince RESTEasy 4.6.0
 */
@RunWith(Arquillian.class)
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
            Assert.assertEquals(expectedStatus.getStatusCode(), response.getStatus());
            Assert.assertEquals(expectedText, response.readEntity(String.class));
        }
    }
}
