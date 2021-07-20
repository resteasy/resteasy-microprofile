package org.jboss.resteasy.microprofile.client;

import static org.jboss.resteasy.microprofile.client.utils.ClientRequestContextUtils.getMethod;

import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;


@Priority(Integer.MIN_VALUE)
public class MethodInjectionFilter implements ClientRequestFilter {
    @Override
    public void filter(ClientRequestContext requestContext) {
        Method method = getMethod(requestContext);
        requestContext.setProperty("org.eclipse.microprofile.rest.client.invokedMethod", method);
    }
}
