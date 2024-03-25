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
import java.util.PropertyPermission;

import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.exception.ResteasyWebApplicationException;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ClientWebApplicationExceptionMicroProfileProxyApplication;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ClientWebApplicationExceptionMicroProfileProxyResource;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ClientWebApplicationExceptionProxyResourceInterface;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.resteasy.utils.PermissionUtil;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @tpSubChapter Resteasy-client
 * @tpChapter Client throws ResteasyWebApplicationException on server side
 * @tpSince RESTEasy 4.6.0.Final
 * @tpTestCaseDetails Test WebApplicationExceptions and WebApplicationExceptionWrappers in various circumstances,
 *                    calls made by MicroProfile REST Client proxies.
 *
 *                    NOTE. Unlike RESTEasy Clients and RESTEasy Client proxies, which throws the subtree of
 *                    WebApplicationExceptions
 *                    and WebApplicationExceptionWrappers, MicroProfile REST Client proxies throw only WebApplicationExceptions
 *                    and WebApplicationExceptionWrappers.
 */
@ExtendWith(ArquillianExtension.class)
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
                .addAsWebInfResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<beans xmlns=\"https://jakarta.ee/xml/ns/jakartaee\"\n" +
                        "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "       xsi:schemaLocation=\"https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_3_0.xsd\"\n"
                        +
                        "       version=\"3.0\" bean-discovery-mode=\"all\">\n" +
                        "</beans>"), "beans.xml")
                .addAsManifestResource(PermissionUtil.createPermissionsXmlAsset(
                        new PropertyPermission("resteasy.original.webapplicationexception.behavior", "write")),
                        "permissions.xml");
    }

    @BeforeEach
    public void createProxy() throws Exception {
        proxy = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "/app/test/"))
                .build(ClientWebApplicationExceptionProxyResourceInterface.class);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @tpTestDetails For each WebApplicationException in oldExceptions, calls the resource method oldException() to throw
     *                that WebApplicationException. Since it is running on the client side, the standard behavior of throwing a
     *                WebApplicationException will occur.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testOldExceptionsDirectly() {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.oldExceptions.length; i++) {
            try {
                proxy.oldException(i);
                Assertions.fail("expected exception");
            } catch (ResteasyWebApplicationException rwae) {
                Assertions.fail("Didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException wae) {
                Response response = wae.getResponse();
                WebApplicationException oldException = ClientWebApplicationExceptionConstants.oldExceptions[i];
                Assertions.assertEquals(oldException.getResponse().getStatus(), response.getStatus());
                Assertions.assertEquals(oldException.getResponse().getHeaderString("foo"), response.getHeaderString("foo"));
                Assertions.assertEquals(oldException.getResponse().getEntity(), response.readEntity(String.class));
                Assertions.assertEquals(WebApplicationException.class, wae.getClass());
            } catch (Exception e) {
                Assertions.fail("expected WebApplicationException");
            }
        }
        // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
        // ClientInvocation creates a RedirectionException.
        try {
            proxy.oldException(0);
            Assertions.fail("expected exception");
        } catch (ResteasyWebApplicationException rwae) {
            Assertions.fail("Didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException wae) {
            Response response = wae.getResponse();
            WebApplicationException oldException = ClientWebApplicationExceptionConstants.oldExceptions[0];
            Assertions.assertEquals(oldException.getResponse().getStatus(), response.getStatus());
            Assertions.assertEquals(oldException.getResponse().getHeaderString("foo"), response.getHeaderString("foo"));
            Assertions.assertEquals(oldException.getResponse().getEntity(), response.readEntity(String.class));
            Assertions.assertEquals(RedirectionException.class, wae.getClass());
        } catch (Exception e) {
            Assertions.fail("expected WebApplicationException");
        }
    }

    /**
     * @tpTestDetails For each ResteasyWebApplicationException in newExceptions, calls the resource method newException() to
     *                throw
     *                that WebApplicationExceptionWrapper. Since it is running on the client side, the standard behavior of
     *                throwing a
     *                WebApplicationException will occur.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testNewExceptionsDirectly() throws Exception {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.newExceptions.length; i++) {
            try {
                proxy.newException(i);
                Assertions.fail("expected exception");
            } catch (ResteasyWebApplicationException rwae) {
                Assertions.fail("Didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                final String msg = String.format("Failed on %d: %s", i, ClientWebApplicationExceptionConstants.newExceptions[i]
                        .getResponse());
                Response response = e.getResponse();
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                        .getStatus(), response.getStatus(), msg);
                Assertions.assertNull(response.getHeaderString("foo"), msg);
                Assertions.assertTrue(response.readEntity(String.class).isEmpty(), msg);
                Assertions.assertEquals(WebApplicationException.class, e.getClass(), msg);
            }
        }
        // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
        // ClientInvocation creates a RedirectionException.
        try {
            proxy.newException(0);
            Assertions.fail("expected exception");
        } catch (ResteasyWebApplicationException rwae) {
            Assertions.fail("Didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException e) {
            final String msg = String.format("Failed on %d: %s", 0, ClientWebApplicationExceptionConstants.newExceptions[0]
                    .getResponse());
            Response response = e.getResponse();
            Assertions.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[0].getResponse()
                    .getStatus(), response.getStatus(), msg);
            Assertions.assertNull(response.getHeaderString("foo"), msg);
            Assertions.assertTrue(response.readEntity(String.class).isEmpty(), msg);
            Assertions.assertEquals(RedirectionException.class, e.getClass(), msg);
        }
    }

    /**
     * @tpTestDetails 1. The value of ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR is
     *                set to "true" to compel the original Client behavior on the server side.
     *
     *                2. For each WebApplicationException in oldExceptions, the resource method noCatchOld() is called.
     *
     *                3. noCatchOld() calls oldException(), which throws the chosen member of oldExceptions. The resulting
     *                HTTP response contains the status, headers, and entity in that WebApplicationException.
     *
     *                4. In noCatchOld(), the original behavior causes the HTTP response to be turned into a
     *                WebApplicationException,
     *                which is thrown by the Client. The resulting HTTP response contains the status, headers, and entity in
     *                that
     *                WebApplicationException.
     *
     *                5. The client side Client constructs and throws a WebApplicationException which is checked against the
     *                matching
     *                WebApplicationException in oldExceptins.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testNoCatchOldBehaviorOldExceptions() {
        proxy.setBehavior("true");
        try {
            for (int i = 1; i < ClientWebApplicationExceptionConstants.oldExceptions.length; i++) {
                try {
                    proxy.noCatchOld(i);
                    Assertions.fail("expected exception");
                } catch (ResteasyWebApplicationException e) {
                    Assertions.fail("didn't expect ResteasyWebApplicationException");
                } catch (WebApplicationException e) {
                    Response response = e.getResponse();
                    WebApplicationException wae = ClientWebApplicationExceptionConstants.oldExceptions[i];
                    Assertions.assertEquals(wae.getResponse().getStatus(), response.getStatus());
                    Assertions.assertEquals(wae.getResponse().getHeaderString("foo"), response.getHeaderString("foo"));
                    Assertions.assertEquals(wae.getResponse().getEntity(), response.readEntity(String.class));
                    Assertions.assertEquals(WebApplicationException.class, e.getClass());
                } catch (Exception e) {
                    Assertions.fail("expected WebApplicationException");
                }
            }
            // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
            // ClientInvocation creates a RedirectionException.
            try {
                proxy.noCatchOld(0);
                Assertions.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assertions.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                WebApplicationException wae = ClientWebApplicationExceptionConstants.oldExceptions[0];
                Assertions.assertEquals(wae.getResponse().getStatus(), response.getStatus());
                Assertions.assertEquals(wae.getResponse().getHeaderString("foo"), response.getHeaderString("foo"));
                Assertions.assertEquals(wae.getResponse().getEntity(), response.readEntity(String.class));
                Assertions.assertEquals(RedirectionException.class, e.getClass());
            } catch (Exception e) {
                Assertions.fail("expected WebApplicationException");
            }
        } finally {
            proxy.setBehavior("false");
        }
    }

    /**
     * @tpTestDetails 1. The value of ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR is
     *                set to "true" to compel the original Client behavior on the server side.
     *
     *                2. For each ResteasyWebApplicationException in ClientWebApplicationExceptionTest.newExceptions, the
     *                resource method noCatchNew() is called.
     *
     *                3. noCatchNew() calls newException(), which throws the matching member of newExceptions. The resulting
     *                Response is sanitized.
     *
     *                4. In noCatchNew(), the original behavior causes the HTTP response to be turned into a
     *                WebApplicationException,
     *                which is thrown by the Client. The resulting HTTP response is sanitized.
     *
     *                5. The client side Client constructs and throws a WebApplicationException which is checked for a sanitized
     *                Response and matching status.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testNoCatchOldBehaviorNewExceptions() {
        proxy.setBehavior("true");
        try {
            for (int i = 1; i < ClientWebApplicationExceptionConstants.newExceptions.length; i++) {
                try {
                    proxy.noCatchNew(i);
                    Assertions.fail("expected exception");
                } catch (ResteasyWebApplicationException e) {
                    Assertions.fail("didn't expect ResteasyWebApplicationException");
                } catch (WebApplicationException e) {
                    Response response = e.getResponse();
                    Assertions.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                            .getStatus(), response.getStatus());
                    Assertions.assertNull(response.getHeaderString("foo"));
                    Assertions.assertTrue(response.readEntity(String.class).isEmpty());
                    Assertions.assertEquals(WebApplicationException.class, e.getClass());
                } catch (Exception e) {
                    Assertions.fail("expected WebApplicationException");
                }
            }
            // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
            // ClientInvocation creates a RedirectionException.
            try {
                proxy.noCatchNew(0);
                Assertions.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assertions.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[0].getResponse()
                        .getStatus(), response.getStatus());
                Assertions.assertNull(response.getHeaderString("foo"));
                Assertions.assertTrue(response.readEntity(String.class).isEmpty());
                Assertions.assertEquals(RedirectionException.class, e.getClass());
            } catch (Exception e) {
                Assertions.fail("expected WebApplicationException");
            }
        } finally {
            proxy.setBehavior("false");
        }
    }

    /**
     * @tpTestDetails 1. For each WebApplicationException in oldExceptions, the resource method noCatchOld() is called.
     *
     *                2. noCatchOld() calls oldException(), which throws the matching member of oldExceptions. The resulting
     *                HTTP response contains the status, headers, and entity in that WebApplicationException.
     *
     *                3. In noCatchOld(), the new behavior causes the HTTP response to be turned into a
     *                WebApplicationExceptionWrapper,
     *                which is thrown by the Client. WebApplicationExceptionWrapper.getResponse() returns a sanitized Response
     *
     *                4. The client side Client constructs and throws a WebApplicationException which is checked for a sanitized
     *                Response and matching status.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testNoCatchNewBehaviorOldExceptions() {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.oldExceptions.length; i++) {
            try {
                proxy.noCatchOld(i);
                Assertions.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assertions.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                        .getStatus(), response.getStatus());
                Assertions.assertNull(response.getHeaderString("foo"));
                Assertions.assertTrue(response.readEntity(String.class).isEmpty());
                Assertions.assertEquals(WebApplicationException.class, e.getClass());
            } catch (Exception e) {
                Assertions.fail("expected WebApplicationException");
            }
        }
        // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
        // ClientInvocation creates a RedirectionException.
        try {
            proxy.noCatchOld(0);
            Assertions.fail("expected exception");
        } catch (ResteasyWebApplicationException e) {
            Assertions.fail("didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                    .getStatus(), response.getStatus());
            Assertions.assertNull(response.getHeaderString("foo"));
            Assertions.assertTrue(response.readEntity(String.class).isEmpty());
            Assertions.assertEquals(RedirectionException.class, e.getClass());
        } catch (Exception e) {
            Assertions.fail("expected WebApplicationException");
        }
    }

    /**
     * @tpTestDetails 1. For each ResteasyWebApplicationException in newExceptions, the resource method noCatchNew() is called.
     *
     *                2. noCatchNew() calls newException(), which throws the matching member of newExceptions.
     *
     *                3. In noCatchNew(), the new behavior causes the HTTP response to be turned into a
     *                WebApplicationExceptionWrapper,
     *                which is thrown by the Client.
     *
     *                4. The client side Client constructs and throws a WebApplicationException which is checked for matching
     *                status, no
     *                added headers, and empty entity.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testNoCatchNewBehaviorNewExceptions() {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.newExceptions.length; i++) {
            try {
                proxy.noCatchNew(i);
                Assertions.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assertions.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                        .getStatus(), response.getStatus());
                Assertions.assertNull(response.getHeaderString("foo"));
                Assertions.assertTrue(response.readEntity(String.class).isEmpty());
                Assertions.assertEquals(WebApplicationException.class, e.getClass());
            } catch (Exception e) {
                Assertions.fail("expected WebApplicationException");
            }
        }
        // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
        // ClientInvocation creates a RedirectionException.
        try {
            proxy.noCatchNew(0);
            Assertions.fail("expected exception");
        } catch (ResteasyWebApplicationException e) {
            Assertions.fail("didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            Assertions.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[0].getResponse()
                    .getStatus(), response.getStatus());
            Assertions.assertNull(response.getHeaderString("foo"));
            Assertions.assertTrue(response.readEntity(String.class).isEmpty());
            Assertions.assertEquals(RedirectionException.class, e.getClass());
        } catch (Exception e) {
            Assertions.fail("expected WebApplicationException");
        }
    }

    /**
     * @tpTestDetails 1. The value of ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR is
     *                set to "true" to compel the original Client behavior on the server side.
     *
     *                2. For each WebApplicationException in oldExceptions, the resource method catchOldOld() is called.
     *
     *                3. catchOldOld() calls oldException(), which throws the chosen member of oldExceptions. The resulting
     *                HTTP response contains the status, headers, and entity in that WebApplicationException.
     *
     *                4. In catchOldOld(), the original behavior causes the HTTP response to be turned into a
     *                WebApplicationException,
     *                which is thrown by the Client. That WebApplicationException is caught, verified to match the matching
     *                WebApplicationException in oldExceptins, and then rethrown. The resulting HTTP response contains the
     *                status, headers, and entity in that WebApplicationException.
     *
     *                5. The client side Client constructs and throws a WebApplicationException which is checked against the
     *                matching
     *                WebApplicationException in oldExceptins.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testCatchOldBehaviorOldExceptions() {
        proxy.setBehavior("true");
        try {
            for (int i = 1; i < ClientWebApplicationExceptionConstants.oldExceptions.length; i++) {
                try {
                    proxy.catchOldOld(i);
                    Assertions.fail("expected exception");
                } catch (ResteasyWebApplicationException e) {
                    Assertions.fail("didn't expect ResteasyWebApplicationException");
                } catch (WebApplicationException e) {
                    Response response = e.getResponse();
                    Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                            .getStatus(), response.getStatus());
                    Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                            .getHeaderString("foo"), response.getHeaderString("foo"));
                    Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                            .getEntity(), response.readEntity(String.class));
                    Assertions.assertEquals(WebApplicationException.class, e.getClass());
                } catch (Exception e) {
                    Assertions.fail("expected WebApplicationException");
                }
            }
            // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
            // ClientInvocation creates a RedirectionException.
            try {
                proxy.catchOldOld(0);
                Assertions.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assertions.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                        .getStatus(), response.getStatus());
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                        .getHeaderString("foo"), response.getHeaderString("foo"));
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                        .getEntity(), response.readEntity(String.class));
                Assertions.assertEquals(RedirectionException.class, e.getClass());
            } catch (Exception e) {
                Assertions.fail("expected WebApplicationException");
            }
        } finally {
            proxy.setBehavior("false");
        }
    }

    /**
     * @tpTestDetails 1. The value of ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR is
     *                set to "true" to compel the original Client behavior on the server side.
     *
     *                2. For each ResteasyWebApplicationException in newExceptions, the resource method catchOldNew() is called.
     *
     *                3. catchOldNew() calls newException(), which throws the chosen member of newExceptions
     *                WebApplicationExceptionWrapper.getResponse() returns a sanitized Response.
     *
     *                4. In catchOldNew(), the original behavior causes the HTTP response to be turned into a
     *                WebApplicationException,
     *                which is thrown by the Client. That WebApplicationException is caught, verified to
     *                have matching status, no added headers, and an empty entity, and then rethrown.
     *
     *                5. The client side Client constructs and throws a WebApplicationException which is verified to have
     *                matching status, no added headers, and an empty entity.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    public void testCatchOldBehaviorNewExceptions() {
        proxy.setBehavior("true");
        try {
            for (int i = 1; i < ClientWebApplicationExceptionConstants.newExceptions.length; i++) {
                try {
                    proxy.catchOldNew(i);
                    Assertions.fail("expected exception");
                } catch (ResteasyWebApplicationException e) {
                    Assertions.fail("didn't expect ResteasyWebApplicationException");
                } catch (WebApplicationException e) {
                    Response response = e.getResponse();
                    Assertions.assertNotNull(response);
                    Assertions.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                            .getStatus(), response.getStatus());
                    Assertions.assertNull(response.getHeaderString("foo"));
                    Assertions.assertTrue(response.readEntity(String.class).length() == 0);
                    Assertions.assertEquals(WebApplicationException.class, e.getClass());
                } catch (Exception e) {
                    Assertions.fail("expected WebApplicationException");
                }
            }
            // DefaultResponseExceptionMapper on client side doesn't handle status s, 300 <= s < 400, and
            // ClientInvocation creates a RedirectionException.
            try {
                proxy.catchOldNew(0);
                Assertions.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assertions.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assertions.assertNotNull(response);
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[0].getResponse()
                        .getStatus(), response.getStatus());
                Assertions.assertNull(response.getHeaderString("foo"));
                Assertions.assertTrue(response.readEntity(String.class).length() == 0);
                Assertions.assertEquals(RedirectionException.class, e.getClass());
            } catch (Exception e) {
                Assertions.fail("expected WebApplicationException");
            }
        } finally {
            proxy.setBehavior("false");
        }
    }

    /**
     * @tpTestDetails 1. For each WebApplicationException in oldExceptions, the resource method catchNewOld() is called.
     *
     *                2. catchNewOld() calls oldException(), which throws the matching member of oldExceptions. The resulting
     *                HTTP response contains the status, headers, and entity in that WebApplicationException.
     *
     *                3. In catchNewOld(), the new behavior causes the HTTP response to be turned into a
     *                WebApplicationExceptionWrapper,
     *                which is thrown by the Client, caught, tested, and rethrown.
     *
     *                4. The client side Client constructs and throws a WebApplicationException which is checked for a sanitized
     *                Response and matching status.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    @Disabled("This test does not work for some reason.")
    public void testCatchNewBehaviorOldExceptions() {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.oldExceptions.length; i++) {
            try {
                proxy.catchNewOld(i);
                Assertions.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assertions.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assertions.assertNotNull(response);
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                        .getStatus(), response.getStatus());
                Assertions.assertNull(response.getHeaderString("foo"));
                Assertions.assertTrue(response.readEntity(String.class).isEmpty());
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getClass(), e.getClass());
            } catch (Exception e) {
                Assertions.fail("expected WebApplicationException");
            }
        }
        try {
            proxy.catchNewOld(0);
            Assertions.fail("expected exception");
        } catch (ResteasyWebApplicationException e) {
            Assertions.fail("didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            Assertions.assertNotNull(response);
            Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                    .getStatus(), response.getStatus());
            Assertions.assertNull(response.getHeaderString("foo"));
            Assertions.assertTrue(response.readEntity(String.class).isEmpty());
            Assertions.assertEquals(RedirectionException.class, e.getClass());
        } catch (Exception e) {
            Assertions.fail("expected WebApplicationException");
        }
    }

    /**
     * @tpTestDetails 1. For each ResteasyWebApplicationException in newExceptions, the resource method catchNewNew() is called.
     *
     *                2. catchNewNew() calls newException(), which throws the matching member of newExceptions.
     *
     *                3. In catchNewNew(), the new behavior causes the HTTP response to be turned into a
     *                WebApplicationExceptionWrapper,
     *                which is thrown by the Client, caught, tested, and rethrown.
     *
     *                4. The client side Client constructs and throws a WebApplicationException which is checked for matching
     *                status, no
     *                added headers, and empty entity.
     * @tpSince RESTEasy 4.6.0.Final
     */
    @Test
    @Disabled("This test does not work for some reason.")
    public void testCatchNewBehaviorNewExceptions() {
        for (int i = 1; i < ClientWebApplicationExceptionConstants.newExceptions.length; i++) {
            try {
                proxy.catchNewNew(i);
                Assertions.fail("expected exception");
            } catch (ResteasyWebApplicationException e) {
                Assertions.fail("didn't expect ResteasyWebApplicationException");
            } catch (WebApplicationException e) {
                Response response = e.getResponse();
                Assertions.assertNotNull(response);
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                        .getStatus(), response.getStatus());
                Assertions.assertNull(response.getHeaderString("foo"));
                Assertions.assertTrue(response.readEntity(String.class).isEmpty());
                Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getClass(), e.getClass());
            } catch (Exception e) {
                Assertions.fail("expected WebApplicationException");
            }
        }
        try {
            proxy.catchNewNew(0);
            Assertions.fail("expected exception");
        } catch (ResteasyWebApplicationException e) {
            Assertions.fail("didn't expect ResteasyWebApplicationException: " + e.getClass());
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            Assertions.assertNotNull(response);
            Assertions.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[0].getResponse()
                    .getStatus(), response.getStatus());
            Assertions.assertNull(response.getHeaderString("foo"));
            Assertions.assertTrue(response.readEntity(String.class).isEmpty());
            Assertions.assertEquals(RedirectionException.class, e.getClass());
        } catch (Exception e) {
            Assertions.fail("expected WebApplicationException");
        }
    }
}
