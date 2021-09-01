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

package org.jboss.resteasy.microprofile.test.client.exception;

import java.net.URL;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.exception.ResteasyWebApplicationException;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ClientWebApplicationExceptionMicroProfileProxyApplication;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ClientWebApplicationExceptionMicroProfileProxyResource;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ClientWebApplicationExceptionProxyResourceInterface;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @tpSubChapter Resteasy-client
 * @tpChapter Client throws ResteasyWebApplicationException on server side
 * @tpSince RESTEasy 4.6.0.Final
 * @tpTestCaseDetails Test WebApplicationExceptions and WebApplicationExceptionWrappers in various circumstances,
 * calls made by MicroProfile REST Client proxies.
 *
 * NOTE. Unlike RESTEasy Clients and RESTEasy Client proxies, which throws the subtree of WebApplicationExceptions
 * and WebApplicationExceptionWrappers, MicroProfile REST Client proxies throw only WebApplicationExceptions
 * and WebApplicationExceptionWrappers.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ClientWebApplicationExceptionMicroProfileProxyTest {

    private ClientWebApplicationExceptionProxyResourceInterface proxy;

    @ArquillianResource
    private URL url;


    @Deployment
    public static Archive<?> deploy() throws Exception {
        return TestEnvironment.createWarWithConfigUrl(ClientWebApplicationExceptionMicroProfileProxyTest.class,
                        ClientWebApplicationExceptionProxyResourceInterface.class, "/app/test/")
                .addClass(ClientWebApplicationExceptionConstants.class)
                .addClass(ClientWebApplicationExceptionMicroProfileProxyApplication.class)
                .addClass(ClientWebApplicationExceptionMicroProfileProxyResource.class)
                .addClass(ClientWebApplicationExceptionProxyResourceInterface.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void createProxy() throws Exception {
        proxy = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "/app/test/"))
                .build(ClientWebApplicationExceptionProxyResourceInterface.class);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @tpTestDetails For each WebApplicationException in oldExceptions, calls the resource method oldException() to throw
     * that WebApplicationException. Since it is running on the client side, the standard behavior of throwing a
     * WebApplicationException will occur.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testOldExceptionsDirectly() {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.oldExceptions.length; i++) {
            try {
                proxy.oldException(i);
                Assert.fail("expected exception");
            } catch (ResteasyWebApplicationException rwae) {
                Assert.fail("Didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException wae) {
                Response response = wae.getResponse();
                WebApplicationException oldException = ClientWebApplicationExceptionConstants.oldExceptions[i];
                Assert.assertEquals(oldException.getResponse().getStatus(), response.getStatus());
                Assert.assertEquals(oldException.getResponse().getHeaderString("foo"), response.getHeaderString("foo"));
                Assert.assertEquals(oldException.getResponse().getEntity(), response.readEntity(String.class));
                Assert.assertEquals(WebApplicationException.class, wae.getClass());
            } catch (Exception e) {
                Assert.fail("expected WebApplicationException");
            }
        }
        // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
        // ClientInvocation creates a RedirectionException.
        try {
            proxy.oldException(0);
            Assert.fail("expected exception");
        } catch (ResteasyWebApplicationException rwae) {
            Assert.fail("Didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException wae) {
            Response response = wae.getResponse();
            WebApplicationException oldException = ClientWebApplicationExceptionConstants.oldExceptions[0];
            Assert.assertEquals(oldException.getResponse().getStatus(), response.getStatus());
            Assert.assertEquals(oldException.getResponse().getHeaderString("foo"), response.getHeaderString("foo"));
            Assert.assertEquals(oldException.getResponse().getEntity(), response.readEntity(String.class));
            Assert.assertEquals(RedirectionException.class, wae.getClass());
        } catch (Exception e) {
            Assert.fail("expected WebApplicationException");
        }
    }

    /**
     * @tpTestDetails For each ResteasyWebApplicationException in newExceptions, calls the resource method newException() to throw
     * that WebApplicationExceptionWrapper. Since it is running on the client side, the standard behavior of throwing a
     * WebApplicationException will occur.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testNewExceptionsDirectly() throws Exception {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.newExceptions.length; i++) {
            try {
                proxy.newException(i);
                Assert.fail("expected exception");
            } catch (ResteasyWebApplicationException rwae) {
                Assert.fail("Didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                final String msg = String.format("Failed on %d: %s", i, ClientWebApplicationExceptionConstants.newExceptions[i]
                        .getResponse());
                Response response = e.getResponse();
                Assert.assertEquals(msg, ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                        .getStatus(), response.getStatus());
                Assert.assertNull(msg, response.getHeaderString("foo"));
                Assert.assertTrue(msg, response.readEntity(String.class).isEmpty());
                Assert.assertEquals(msg, WebApplicationException.class, e.getClass());
            }
        }
        // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
        // ClientInvocation creates a RedirectionException.
        try {
            proxy.newException(0);
            Assert.fail("expected exception");
        } catch (ResteasyWebApplicationException rwae) {
            Assert.fail("Didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException e) {
            final String msg = String.format("Failed on %d: %s", 0, ClientWebApplicationExceptionConstants.newExceptions[0]
                    .getResponse());
            Response response = e.getResponse();
            Assert.assertEquals(msg, ClientWebApplicationExceptionConstants.newExceptions[0].getResponse()
                    .getStatus(), response.getStatus());
            Assert.assertNull(msg, response.getHeaderString("foo"));
            Assert.assertTrue(msg, response.readEntity(String.class).isEmpty());
            Assert.assertEquals(msg, RedirectionException.class, e.getClass());
        }
    }

    /**
     * @tpTestDetails 1. The value of ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR is
     * set to "true" to compel the original Client behavior on the server side.
     *
     * 2. For each WebApplicationException in oldExceptions, the resource method noCatchOld() is called.
     *
     * 3. noCatchOld() calls oldException(), which throws the chosen member of oldExceptions. The resulting
     * HTTP response contains the status, headers, and entity in that WebApplicationException.
     *
     * 4. In noCatchOld(), the original behavior causes the HTTP response to be turned into a WebApplicationException,
     * which is thrown by the Client. The resulting HTTP response contains the status, headers, and entity in that
     * WebApplicationException.
     *
     * 5. The client side Client constructs and throws a WebApplicationException which is checked against the matching
     * WebApplicationException in oldExceptins.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testNoCatchOldBehaviorOldExceptions() {
        proxy.setBehavior("true");
        try {
            for (int i = 1; i < ClientWebApplicationExceptionConstants.oldExceptions.length; i++) {
                try {
                    proxy.noCatchOld(i);
                    Assert.fail("expected exception");
                } catch (ResteasyWebApplicationException e) {
                    Assert.fail("didn't expect ResteasyWebApplicationException");
                } catch (WebApplicationException e) {
                    Response response = e.getResponse();
                    WebApplicationException wae = ClientWebApplicationExceptionConstants.oldExceptions[i];
                    Assert.assertEquals(wae.getResponse().getStatus(), response.getStatus());
                    Assert.assertEquals(wae.getResponse().getHeaderString("foo"), response.getHeaderString("foo"));
                    Assert.assertEquals(wae.getResponse().getEntity(), response.readEntity(String.class));
                    Assert.assertEquals(WebApplicationException.class, e.getClass());
                } catch (Exception e) {
                    Assert.fail("expected WebApplicationException");
                }
            }
            // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
            // ClientInvocation creates a RedirectionException.
            try {
                proxy.noCatchOld(0);
                Assert.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assert.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                WebApplicationException wae = ClientWebApplicationExceptionConstants.oldExceptions[0];
                Assert.assertEquals(wae.getResponse().getStatus(), response.getStatus());
                Assert.assertEquals(wae.getResponse().getHeaderString("foo"), response.getHeaderString("foo"));
                Assert.assertEquals(wae.getResponse().getEntity(), response.readEntity(String.class));
                Assert.assertEquals(RedirectionException.class, e.getClass());
            } catch (Exception e) {
                Assert.fail("expected WebApplicationException");
            }
        } finally {
            proxy.setBehavior("false");
        }
    }

    /**
     * @tpTestDetails 1. The value of ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR is
     * set to "true" to compel the original Client behavior on the server side.
     *
     * 2. For each ResteasyWebApplicationException in ClientWebApplicationExceptionTest.newExceptions, the resource method noCatchNew() is called.
     *
     * 3. noCatchNew() calls newException(), which throws the matching member of newExceptions. The resulting
     * Response is sanitized.
     *
     * 4. In noCatchNew(), the original behavior causes the HTTP response to be turned into a WebApplicationException,
     * which is thrown by the Client. The resulting HTTP response is sanitized.
     *
     * 5. The client side Client constructs and throws a WebApplicationException which is checked for a sanitized
     * Response and matching status.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testNoCatchOldBehaviorNewExceptions() {
        proxy.setBehavior("true");
        try {
            for (int i = 1; i < ClientWebApplicationExceptionConstants.newExceptions.length; i++) {
                try {
                    proxy.noCatchNew(i);
                    Assert.fail("expected exception");
                } catch (ResteasyWebApplicationException e) {
                    Assert.fail("didn't expect ResteasyWebApplicationException");
                } catch (WebApplicationException e) {
                    Response response = e.getResponse();
                    Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                            .getStatus(), response.getStatus());
                    Assert.assertNull(response.getHeaderString("foo"));
                    Assert.assertTrue(response.readEntity(String.class).isEmpty());
                    Assert.assertEquals(WebApplicationException.class, e.getClass());
                } catch (Exception e) {
                    Assert.fail("expected WebApplicationException");
                }
            }
            // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
            // ClientInvocation creates a RedirectionException.
            try {
                proxy.noCatchNew(0);
                Assert.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assert.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[0].getResponse()
                        .getStatus(), response.getStatus());
                Assert.assertNull(response.getHeaderString("foo"));
                Assert.assertTrue(response.readEntity(String.class).isEmpty());
                Assert.assertEquals(RedirectionException.class, e.getClass());
            } catch (Exception e) {
                Assert.fail("expected WebApplicationException");
            }
        } finally {
            proxy.setBehavior("false");
        }
    }

    /**
     * @tpTestDetails 1. For each WebApplicationException in oldExceptions, the resource method noCatchOld() is called.
     *
     * 2. noCatchOld() calls oldException(), which throws the matching member of oldExceptions. The resulting
     * HTTP response contains the status, headers, and entity in that WebApplicationException.
     *
     * 3. In noCatchOld(), the new behavior causes the HTTP response to be turned into a WebApplicationExceptionWrapper,
     * which is thrown by the Client. WebApplicationExceptionWrapper.getResponse() returns a sanitized Response
     *
     * 4. The client side Client constructs and throws a WebApplicationException which is checked for a sanitized
     * Response and matching status.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testNoCatchNewBehaviorOldExceptions() {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.oldExceptions.length; i++) {
            try {
                proxy.noCatchOld(i);
                Assert.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assert.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                        .getStatus(), response.getStatus());
                Assert.assertNull(response.getHeaderString("foo"));
                Assert.assertTrue(response.readEntity(String.class).isEmpty());
                Assert.assertEquals(WebApplicationException.class, e.getClass());
            } catch (Exception e) {
                Assert.fail("expected WebApplicationException");
            }
        }
        // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
        // ClientInvocation creates a RedirectionException.
        try {
            proxy.noCatchOld(0);
            Assert.fail("expected exception");
        } catch (ResteasyWebApplicationException e) {
            Assert.fail("didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                    .getStatus(), response.getStatus());
            Assert.assertNull(response.getHeaderString("foo"));
            Assert.assertTrue(response.readEntity(String.class).isEmpty());
            Assert.assertEquals(RedirectionException.class, e.getClass());
        } catch (Exception e) {
            Assert.fail("expected WebApplicationException");
        }
    }

    /**
     * @tpTestDetails 1. For each ResteasyWebApplicationException in newExceptions, the resource method noCatchNew() is called.
     *
     * 2. noCatchNew() calls newException(), which throws the matching member of newExceptions.
     *
     * 3. In noCatchNew(), the new behavior causes the HTTP response to be turned into a WebApplicationExceptionWrapper,
     * which is thrown by the Client.
     *
     * 4. The client side Client constructs and throws a WebApplicationException which is checked for matching status, no
     * added headers, and empty entity.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testNoCatchNewBehaviorNewExceptions() {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.newExceptions.length; i++) {
            try {
                proxy.noCatchNew(i);
                Assert.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assert.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                        .getStatus(), response.getStatus());
                Assert.assertNull(response.getHeaderString("foo"));
                Assert.assertTrue(response.readEntity(String.class).isEmpty());
                Assert.assertEquals(WebApplicationException.class, e.getClass());
            } catch (Exception e) {
                Assert.fail("expected WebApplicationException");
            }
        }
        // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
        // ClientInvocation creates a RedirectionException.
        try {
            proxy.noCatchNew(0);
            Assert.fail("expected exception");
        } catch (ResteasyWebApplicationException e) {
            Assert.fail("didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[0].getResponse()
                    .getStatus(), response.getStatus());
            Assert.assertNull(response.getHeaderString("foo"));
            Assert.assertTrue(response.readEntity(String.class).isEmpty());
            Assert.assertEquals(RedirectionException.class, e.getClass());
        } catch (Exception e) {
            Assert.fail("expected WebApplicationException");
        }
    }

    /**
     * @tpTestDetails 1. The value of ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR is
     * set to "true" to compel the original Client behavior on the server side.
     *
     * 2. For each WebApplicationException in oldExceptions, the resource method catchOldOld() is called.
     *
     * 3. catchOldOld() calls oldException(), which throws the chosen member of oldExceptions. The resulting
     * HTTP response contains the status, headers, and entity in that WebApplicationException.
     *
     * 4. In catchOldOld(), the original behavior causes the HTTP response to be turned into a WebApplicationException,
     * which is thrown by the Client. That WebApplicationException is caught, verified to match the matching
     * WebApplicationException in oldExceptins, and then rethrown. The resulting HTTP response contains the
     * status, headers, and entity in that WebApplicationException.
     *
     * 5. The client side Client constructs and throws a WebApplicationException which is checked against the matching
     * WebApplicationException in oldExceptins.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testCatchOldBehaviorOldExceptions() {
        proxy.setBehavior("true");
        try {
            for (int i = 1; i < ClientWebApplicationExceptionConstants.oldExceptions.length; i++) {
                try {
                    proxy.catchOldOld(i);
                    Assert.fail("expected exception");
                } catch (ResteasyWebApplicationException e) {
                    Assert.fail("didn't expect ResteasyWebApplicationException");
                } catch (WebApplicationException e) {
                    Response response = e.getResponse();
                    Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                            .getStatus(), response.getStatus());
                    Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                            .getHeaderString("foo"), response.getHeaderString("foo"));
                    Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                            .getEntity(), response.readEntity(String.class));
                    Assert.assertEquals(WebApplicationException.class, e.getClass());
                } catch (Exception e) {
                    Assert.fail("expected WebApplicationException");
                }
            }
            // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
            // ClientInvocation creates a RedirectionException.
            try {
                proxy.catchOldOld(0);
                Assert.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assert.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                        .getStatus(), response.getStatus());
                Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                        .getHeaderString("foo"), response.getHeaderString("foo"));
                Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                        .getEntity(), response.readEntity(String.class));
                Assert.assertEquals(RedirectionException.class, e.getClass());
            } catch (Exception e) {
                Assert.fail("expected WebApplicationException");
            }
        } finally {
            proxy.setBehavior("false");
        }
    }

    /**
     * @tpTestDetails 1. The value of ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR is
     * set to "true" to compel the original Client behavior on the server side.
     *
     * 2. For each ResteasyWebApplicationException in newExceptions, the resource method catchOldNew() is called.
     *
     * 3. catchOldNew() calls newException(), which throws the chosen member of newExceptions
     * WebApplicationExceptionWrapper.getResponse() returns a sanitized Response.
     *
     * 4. In catchOldNew(), the original behavior causes the HTTP response to be turned into a WebApplicationException,
     * which is thrown by the Client. That WebApplicationException is caught, verified to
     * have matching status, no added headers, and an empty entity, and then rethrown.
     *
     * 5. The client side Client constructs and throws a WebApplicationException which is verified to have
     * matching status, no added headers, and an empty entity.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testCatchOldBehaviorNewExceptions() {
        proxy.setBehavior("true");
        try {
            for (int i = 1; i < ClientWebApplicationExceptionConstants.newExceptions.length; i++) {
                try {
                    proxy.catchOldNew(i);
                    Assert.fail("expected exception");
                } catch (ResteasyWebApplicationException e) {
                    Assert.fail("didn't expect ResteasyWebApplicationException");
                } catch (WebApplicationException e) {
                    Response response = e.getResponse();
                    Assert.assertNotNull(response);
                    Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                            .getStatus(), response.getStatus());
                    Assert.assertNull(response.getHeaderString("foo"));
                    Assert.assertTrue(response.readEntity(String.class).length() == 0);
                    Assert.assertEquals(WebApplicationException.class, e.getClass());
                } catch (Exception e) {
                    Assert.fail("expected WebApplicationException");
                }
            }
            // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
            // ClientInvocation creates a RedirectionException.
            try {
                proxy.catchOldNew(0);
                Assert.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assert.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assert.assertNotNull(response);
                Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[0].getResponse()
                        .getStatus(), response.getStatus());
                Assert.assertNull(response.getHeaderString("foo"));
                Assert.assertTrue(response.readEntity(String.class).length() == 0);
                Assert.assertEquals(RedirectionException.class, e.getClass());
            } catch (Exception e) {
                Assert.fail("expected WebApplicationException");
            }
        } finally {
            proxy.setBehavior("false");
        }
    }

    /**
     * @tpTestDetails 1. For each WebApplicationException in oldExceptions, the resource method catchNewOld() is called.
     *
     * 2. catchNewOld() calls oldException(), which throws the matching member of oldExceptions. The resulting
     * HTTP response contains the status, headers, and entity in that WebApplicationException.
     *
     * 3. In catchNewOld(), the new behavior causes the HTTP response to be turned into a WebApplicationExceptionWrapper,
     * which is thrown by the Client, caught, tested, and rethrown.
     *
     * 4. The client side Client constructs and throws a WebApplicationException which is checked for a sanitized
     * Response and matching status.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    @Ignore("This test does not work for some reason.")
    public void testCatchNewBehaviorOldExceptions() {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.oldExceptions.length; i++) {
            try {
                proxy.catchNewOld(i);
                Assert.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assert.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assert.assertNotNull(response);
                Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                        .getStatus(), response.getStatus());
                Assert.assertNull(response.getHeaderString("foo"));
                Assert.assertTrue(response.readEntity(String.class).isEmpty());
                Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getClass(), e.getClass());
            } catch (Exception e) {
                Assert.fail("expected WebApplicationException");
            }
        }
        try {
            proxy.catchNewOld(0);
            Assert.fail("expected exception");
        } catch (ResteasyWebApplicationException e) {
            Assert.fail("didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            Assert.assertNotNull(response);
            Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                    .getStatus(), response.getStatus());
            Assert.assertNull(response.getHeaderString("foo"));
            Assert.assertTrue(response.readEntity(String.class).isEmpty());
            Assert.assertEquals(RedirectionException.class, e.getClass());
        } catch (Exception e) {
            Assert.fail("expected WebApplicationException");
        }
    }

    /**
     * @tpTestDetails 1. For each ResteasyWebApplicationException in newExceptions, the resource method catchNewNew() is called.
     *
     * 2. catchNewNew() calls newException(), which throws the matching member of newExceptions.
     *
     * 3. In catchNewNew(), the new behavior causes the HTTP response to be turned into a WebApplicationExceptionWrapper,
     * which is thrown by the Client, caught, tested, and rethrown.
     *
     * 4. The client side Client constructs and throws a WebApplicationException which is checked for matching status, no
     * added headers, and empty entity.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    @Ignore("This test does not work for some reason.")
    public void testCatchNewBehaviorNewExceptions() {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.newExceptions.length; i++) {
            try {
                proxy.catchNewNew(i);
                Assert.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assert.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assert.assertNotNull(response);
                Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                        .getStatus(), response.getStatus());
                Assert.assertNull(response.getHeaderString("foo"));
                Assert.assertTrue(response.readEntity(String.class).isEmpty());
                Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getClass(), e.getClass());
            } catch (Exception e) {
                Assert.fail("expected WebApplicationException");
            }
        }
        try {
            proxy.catchNewNew(0);
            Assert.fail("expected exception");
        } catch (ResteasyWebApplicationException e) {
            Assert.fail("didn't expect ResteasyWebApplicationException: " + e.getClass());
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            Assert.assertNotNull(response);
            Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                    .getStatus(), response.getStatus());
            Assert.assertNull(response.getHeaderString("foo"));
            Assert.assertTrue(response.readEntity(String.class).isEmpty());
            Assert.assertEquals(RedirectionException.class, e.getClass());
        } catch (Exception e) {
            Assert.fail("expected WebApplicationException");
        }
    }
}
