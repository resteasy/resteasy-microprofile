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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.RestClientDefinitionException;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 *         2020-07-10
 */
public class HeaderUtils {
    private HeaderUtils() {
    }

    /**
     * Create method handle to call a default method
     *
     * @param method      method to create the handle for
     * @param clientProxy proxy of the rest client
     *
     * @return method handle
     */
    public static MethodHandle createMethodHandle(final Method method, final Object clientProxy) {
        return JdkSpecific.createMethodHandle(method, clientProxy);
    }

    /**
     * resolve method of a given name in a given interface class
     *
     * @param methodSpecifier [fully.quallified.ClassName.]methodName
     * @param interfaceClass  class of the interface, on which the method was defined
     * @param headerName      name of the header for which the method should be called
     *
     * @return method to be called
     */
    public static Method resolveMethod(String methodSpecifier,
            Class<?> interfaceClass,
            String headerName) {
        int lastDot = methodSpecifier.lastIndexOf('.');
        if (lastDot == methodSpecifier.length()) {
            throw new RestClientDefinitionException("Invalid string to specify method: " + methodSpecifier +
                    " for header: '" + headerName + "' on class " + interfaceClass.getCanonicalName());
        }
        String methodName;
        Class<?> clazz;
        if (lastDot > -1) { // class.method specified
            methodName = methodSpecifier.substring(lastDot + 1);

            String className = methodSpecifier.substring(0, lastDot);
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                clazz = Class.forName(className, true, loader);
            } catch (ClassNotFoundException e) {
                throw new RestClientDefinitionException("No class '" + className + "' found " +
                        "for filling header '" + headerName + " on " + interfaceClass.getCanonicalName(),
                        e);
            }
        } else {
            clazz = interfaceClass;
            methodName = methodSpecifier;
        }

        Method method = null;
        boolean resolved = false;
        try {
            method = clazz.getMethod(methodName);
            resolved = true;
        } catch (NoSuchMethodException ignored) {
        }
        if (!resolved) {
            try {
                method = clazz.getMethod(methodName, String.class);
                resolved = true;
            } catch (NoSuchMethodException ignored) {
            }
        }
        if (resolved) {
            return method;
        } else {
            throw new RestClientDefinitionException("Could not resolve method '" + methodSpecifier
                    + "' for filling header '" + headerName + " on " + interfaceClass.getCanonicalName());
        }
    }

    /**
     * casts List&lt;?&gt; to List of Strings
     *
     * @param result list of unknown type
     *
     * @return list of strings
     */
    public static List<String> castListToListOfStrings(List<?> result) {
        return result.stream()
                .map(val -> val instanceof String
                        ? (String) val
                        : String.valueOf(val))
                .collect(Collectors.toList());
    }
}
