package org.jboss.resteasy.microprofile.test.client.exception.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.client.exception.ResteasyWebApplicationException;
import org.jboss.resteasy.client.exception.WebApplicationExceptionWrapper;
import org.jboss.resteasy.microprofile.test.client.exception.ClientWebApplicationExceptionConstants;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.junit.Assert;

@Path("test")
public class ClientWebApplicationExceptionMicroProfileProxyResource {

    @Inject
    @RestClient
    private ClientWebApplicationExceptionProxyResourceInterface proxy;

    /**
     * Sets the System property ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR
     *
     * @param value value property is set to
     */
    @GET
    @Path("behavior/{value}")
    public void setBehavior(@PathParam("value") String value) {
        System.setProperty(ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR, value);
    }

    /**
     * Throws an instance of WebApplicationException from oldExceptions table. The Response returned by
     * WebApplicationException.getResponse() will be used by the container to create an HTTP response.
     *
     * @param i determines element of oldExceptions to be thrown
     *
     * @throws Exception
     */
    @GET
    @Path("exception/old/{i}")
    public String oldException(@PathParam("i") int i) throws Exception {
        throw ClientWebApplicationExceptionConstants.oldExceptions[i];
    }

    /**
     * Throws an instance of ResteasyWebApplicationException from newExceptions table.
     * ResteasyWebApplicationException.getResponse() returns a sanitized response.
     *
     * @param i determines element of newExceptions to be thrown
     *
     * @throws Exception
     */
    @GET
    @Path("exception/new/{i}")
    public String newException(@PathParam("i") int i) throws Exception {
        throw ClientWebApplicationExceptionConstants.newExceptions[i];
    }

    /**
     * Uses a proxy to call oldException() to get an HTTP response derived from a WebApplicationException.
     * Based on that response, the proxy will throw either a WebApplicationException or WebApplicationExceptionWrapper,
     * depending on the value of ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR.
     *
     * @param i determines element of oldExceptions to be thrown by oldException()
     *
     * @throws Exception
     */
    @GET
    @Path("nocatch/old/{i}")
    public String noCatchOld(@PathParam("i") int i) throws Exception {
        return proxy.oldException(i);
    }

    /**
     * Uses a proxy to call newException() to get an HTTP response derived from a WebApplicationExceptionWrapper.
     * Based on that response, the proxy will throw either a WebApplicationException or WebApplicationExceptionWrapper,
     * depending on the value of ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR.
     *
     * @param i determines element of newExceptions to be thrown by newException()
     *
     * @throws Exception
     */
    @GET
    @Path("nocatch/new/{i}")
    public String noCatchNew(@PathParam("i") int i) throws Exception {
        return proxy.newException(i);
    }

    /**
     * It is assumed that ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR is
     * set to "true" before this method is invoked.
     *
     * Uses a proxy to call oldException(). Since the old behavior is configured, the proxy will throw a
     * WebApplicationException, which is caught and examined. The contents should match the WebApplicationException
     * thrown by oldException(). That WebApplicationException is then rethrown.
     *
     * @param i determines element of oldExceptions to be thrown by oldException()
     *
     * @throws Exception
     */
    @GET
    @Path("catch/old/old/{i}")
    public String catchOldOld(@PathParam("i") int i) throws Exception {
        try {
            proxy.oldException(i);
            throw new Exception("expected exception");
        } catch (ResteasyWebApplicationException e) {
            throw new Exception("didn't expect ResteasyWebApplicationException", e);
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                    .getStatus(), response.getStatus());
            Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                    .getHeaderString("foo"), response.getHeaderString("foo"));
            Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                    .getEntity(), response.readEntity(String.class));
            throw e;
        } catch (Exception e) {
            throw new Exception("expected WebApplicationException, not " + e.getClass());
        }
    }

    /**
     * It is assumed that ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR is
     * set to "true" before this method is invoked.
     *
     * Uses a proxy to call newException(). Since the old behavior is configured, the proxy will throw a
     * WebApplicationException, which is caught, examined, and rethrown.
     *
     * @param i determines element of newExceptions to be thrown by newException()
     *
     * @throws Exception
     */
    @GET
    @Path("catch/old/new/{i}")
    public String catchOldNew(@PathParam("i") int i) throws Exception {
        try {
            return proxy.newException(i);
        } catch (ResteasyWebApplicationException e) {
            throw new Exception("didn't expect ResteasyWebApplicationException", e);
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            Assert.assertNotNull(response);
            Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                    .getStatus(), response.getStatus());
            Assert.assertNull(response.getHeaderString("foo"));
            Assert.assertTrue(response.readEntity(String.class).length() == 0);
            throw e;
        } catch (Exception e) {
            throw new Exception("expected WebApplicationException, not " + e.getClass());
        }
    }

    /**
     * It is assumed that ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR holds
     * "false" when this method is invoked.
     *
     * Uses a Client to call oldException().  Since the new behavior is configured, the proxy will throw a
     * WebApplicationExceptionWrapper, which is caught and examined. getResponse() should return a sanitized
     * Response, but the unwrapped Response should match the WebApplicationException
     * thrown by oldException(). That WebApplicationExceptionWrapper is then rethrown.
     *
     * @param i determines element of oldExceptions to be thrown by oldException()
     *
     * @throws Exception
     */
    @GET
    @Path("catch/new/old/{i}")
    public String catchNewOld(@PathParam("i") int i) throws Exception {
        try {
            return proxy.oldException(i);
        } catch (ResteasyWebApplicationException e) {
            throw new Exception("didn't expect ResteasyWebApplicationException", e);
        } catch (WebApplicationException e) {
            Response sanitizedResponse = e.getResponse();
            Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                    .getStatus(), sanitizedResponse.getStatus());
            Assert.assertNull(sanitizedResponse.getHeaderString("foo"));
            Assert.assertFalse(sanitizedResponse.hasEntity());
            Response originalResponse = WebApplicationExceptionWrapper.unwrap(e).getResponse();
            Assert.assertNotNull(originalResponse);
            Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                    .getStatus(), originalResponse.getStatus());
            Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                    .getHeaderString("foo"), originalResponse.getHeaderString("foo"));
            Assert.assertEquals(ClientWebApplicationExceptionConstants.oldExceptions[i].getResponse()
                    .getEntity(), originalResponse.readEntity(String.class));
            Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptionMap.get(originalResponse.getStatus()), e
                    .getClass());
            throw e;
        } catch (Exception e) {
            throw new Exception("expected WebApplicationException, not " + e.getClass());
        }
    }

    /**
     * It is assumed that ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR holds
     * "false" when this method is invoked.
     *
     * Uses a Client to call newException(). Since the new behavior is configured, the proxy will throw a
     * WebApplicationExceptionWrapper, which is caught and examined. getResponse() should return a sanitized
     * Response, but the unwrapped Response should match the WebApplicationException
     * thrown by newException(). That WebApplicationExceptionWrapper is then rethrown.
     *
     * @param i determines element of newExceptions to be thrown by newException()
     *
     * @throws Exception
     */
    @GET
    @Path("catch/new/new/{i}")
    public String catchNewNew(@PathParam("i") int i) throws Exception {
        try {
            return proxy.newException(i);
        } catch (ResteasyWebApplicationException e) {
            throw new Exception("didn't expect ResteasyWebApplicationException");
        } catch (WebApplicationException e) {
            Response sanitizedResponse = e.getResponse();
            Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                    .getStatus(), sanitizedResponse.getStatus());
            Assert.assertNull(sanitizedResponse.getHeaderString("foo"));
            Assert.assertFalse(sanitizedResponse.hasEntity());
            Response originalResponse = WebApplicationExceptionWrapper.unwrap(e).getResponse();
            Assert.assertNotNull(originalResponse);
            Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                    .getStatus(), originalResponse.getStatus());
            Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptions[i].getResponse()
                    .getHeaderString("foo"), originalResponse.getHeaderString("foo"));
            Assert.assertTrue(originalResponse.readEntity(String.class).isEmpty());
            Assert.assertEquals(ClientWebApplicationExceptionConstants.newExceptionMap.get(originalResponse.getStatus()), e
                    .getClass());
            throw e;
        } catch (Exception e) {
            throw new Exception("expected WebApplicationException, not " + e.getClass());
        }
    }
}
