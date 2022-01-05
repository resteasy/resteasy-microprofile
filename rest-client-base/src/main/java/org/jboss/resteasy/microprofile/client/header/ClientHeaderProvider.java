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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

public class ClientHeaderProvider {

    static Optional<ClientHeaderProvider> forMethod(final Method method, final Object clientProxy,
            HeaderFillerFactory fillerFactory) {
        Class<?> declaringClass = method.getDeclaringClass();

        ClientHeaderParam[] methodAnnotations = method.getAnnotationsByType(ClientHeaderParam.class);
        ClientHeaderParam[] classAnnotations = declaringClass.getAnnotationsByType(ClientHeaderParam.class);

        Map<String, ClientHeaderGenerator> generators = new HashMap<>();

        for (ClientHeaderParam annotation : methodAnnotations) {
            if (generators.containsKey(annotation.name())) {
                throw new RestClientDefinitionException("Duplicate " + ClientHeaderParam.class.getSimpleName() +
                        " annotation definitions found on " + method);
            }
            generators.put(annotation.name(),
                    new ClientHeaderGenerator(annotation, declaringClass, clientProxy, fillerFactory));
        }

        checkForDuplicateClassLevelAnnotations(classAnnotations, declaringClass);

        Stream.of(classAnnotations)
                .filter(a -> !generators.containsKey(a.name()))
                .forEach(a -> generators.put(a.name(),
                        new ClientHeaderGenerator(a, declaringClass, clientProxy, fillerFactory)));

        return generators.isEmpty()
                ? Optional.empty()
                : Optional.of(new ClientHeaderProvider(generators.values()));
    }

    private static void checkForDuplicateClassLevelAnnotations(final ClientHeaderParam[] classAnnotations,
            final Class<?> declaringClass) {
        Set<String> headerNames = new HashSet<>();
        Arrays.stream(classAnnotations)
                .map(ClientHeaderParam::name)
                .forEach(
                        name -> {
                            if (!headerNames.add(name)) {
                                throw new RestClientDefinitionException(
                                        "Duplicate ClientHeaderParam definition for header name " + name + " on class "
                                                + declaringClass.getCanonicalName());
                            }
                        });
    }

    private final Collection<ClientHeaderGenerator> generators;

    ClientHeaderProvider(final Collection<ClientHeaderGenerator> generators) {
        this.generators = generators;
    }

    /**
     * add headers to headers map
     *
     * @param headers map to add headers to
     */
    public void addHeaders(MultivaluedMap<String, String> headers) {
        generators.forEach(g -> g.fillHeaders(headers));
    }

}
