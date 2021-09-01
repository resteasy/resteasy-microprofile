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

package org.jboss.resteasy.microprofile.client.header;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

/**
 * Creates a value for a specific header (with the specified headerName)
 *
 * Can either provide a static list of values or a computed value, depending on the {@link ClientHeaderParam} annotation
 * it's generated from.
 */
class ClientHeaderGenerator {
    private final boolean required;
    private final String headerName;
    private final HeaderFiller filler;
    private final List<String> staticValues;

    ClientHeaderGenerator(final ClientHeaderParam anno, final Class<?> interfaceClass, final Object clientProxy,
                          final HeaderFillerFactory fillerFactory) {
        headerName = anno.name();
        required = anno.required();
        String[] values = anno.value();
        if (values.length == 0) {
            throw new RestClientDefinitionException("No value provided for " + ClientHeaderParam.class.getSimpleName()
                    + " on " + interfaceClass + " for '" + headerName + "'");
        } else if (values.length == 1 && isMethodCall(values[0])) {
            filler = fillerFactory.createFiller(values[0], headerName, required, interfaceClass, clientProxy);
            staticValues = null;
        } else {
            checkForMethodCallsInHeaderValues(values, interfaceClass, headerName);
            staticValues = Arrays.asList(values);
            filler = null;
        }
    }

    private void checkForMethodCallsInHeaderValues(final String[] values, final Class<?> location,
                                                   final String headerName) {
        if (Stream.of(values).anyMatch(this::isMethodCall)) {
            throw new RestClientDefinitionException("A method call defined as one multiple values for header on "
                    + location.getSimpleName() + " for header '" + headerName + "'");
        }
    }

    private boolean isMethodCall(final String headerValue) {
        return headerValue != null
                && headerValue.startsWith("{")
                && headerValue.endsWith("}");
    }

    public void fillHeaders(final MultivaluedMap<String, String> headers) {
        List<String> headerValues =
                filler != null
                        ? filler.generateValues()
                        : staticValues;

        if (!headerValues.isEmpty() && headers.get(headerName) == null) {
            headers.put(headerName, headerValues);
        }
    }
}
