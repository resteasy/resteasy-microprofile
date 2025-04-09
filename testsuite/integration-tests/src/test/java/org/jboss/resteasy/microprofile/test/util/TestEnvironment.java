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

package org.jboss.resteasy.microprofile.test.util;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.jboss.resteasy.microprofile.test.TestApplication;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class TestEnvironment {

    public static WebArchive createWar(final String deploymentName) {
        return ShrinkWrap.create(WebArchive.class, deploymentName + ".war")
                .addClass(TestApplication.class);
    }

    public static WebArchive createWar(final String deploymentName, final Map<String, String> properties) throws IOException {
        return addConfigProperties(createWar(deploymentName), properties);
    }

    public static WebArchive createWar(final Class<?> test) {
        return createWar(test.getSimpleName());
    }

    public static WebArchive createWar(final Class<?> test, final String nameSuffix) {
        return createWar(test.getSimpleName() + nameSuffix);
    }

    public static WebArchive createWarWithConfigUrl(final Class<?> test, final Class<?> resource) throws IOException {
        return createWarWithConfigUrl(test, resource, null);
    }

    public static WebArchive createWarWithConfigUrl(final Class<?> test, final Class<?> resource, final String path)
            throws IOException {
        final String url = getHttpUrl() + test.getSimpleName()
                + (path == null ? "" : (path.charAt(0) == '/' ? path : "/" + path));
        return addConfigProperties(createWar(test),
                Collections.singletonMap(resource.getName() + "/mp-rest/url", url));
    }

    public static WebArchive createWarWithConfigUrl(final String deploymentName, final Class<?> resource,
            final String path) throws IOException {
        final String url = getHttpUrl() + deploymentName + (path == null ? "" : (path.charAt(0) == '/' ? path : "/" + path));
        return addConfigProperties(createWar(deploymentName),
                Collections.singletonMap(resource.getCanonicalName() + "/mp-rest/url", url));
    }

    public static WebArchive createWarWithConfigUrl(final Class<?> test, final String deploymentNameSuffix,
            final Class<?> resource, final String path) throws IOException {
        final String url = getHttpUrl() + test.getSimpleName()
                + (path == null ? "" : (path.charAt(0) == '/' ? path : "/" + path));
        return addConfigProperties(createWar(test, deploymentNameSuffix),
                Collections.singletonMap(resource.getCanonicalName() + "/mp-rest/url", url));
    }

    public static <T extends Archive<T>> T addConfigProperties(final T archive, final Map<String, String> props)
            throws IOException {
        try (StringWriter writer = new StringWriter()) {
            for (Map.Entry<String, String> entry : props.entrySet()) {
                writer.write(entry.getKey());
                writer.write('=');
                writer.write(entry.getValue());
                writer.write(System.lineSeparator());
            }
            return archive.add(new StringAsset(writer.toString()), "META-INF/microprofile-config.properties");
        }
    }

    public static URI generateUri(final URL base, final String path) throws URISyntaxException {
        if (path == null || path.isEmpty()) {
            return base.toURI();
        }
        final StringBuilder builder = new StringBuilder(base.toString());
        if (builder.charAt(builder.length() - 1) == '/') {
            if (path.charAt(0) == '/') {
                builder.append(path.substring(1));
            } else {
                builder.append(path);
            }
        } else if (path.charAt(0) == '/') {
            builder.append(path.substring(1));
        } else {
            builder.append('/').append(path);
        }
        return new URI(builder.toString());
    }

    public static URI generateUri(final URL base, final String... paths) throws URISyntaxException {
        final StringBuilder builder = new StringBuilder(base.toString());
        if (builder.charAt(builder.length() - 1) == '/') {
            builder.setLength(builder.length() - 1);
        }
        for (String path : paths) {
            if (path.charAt(0) != '/') {
                builder.append('/');
            }
            builder.append(path);
        }
        return new URI(builder.toString());
    }

    public static String getHost() {
        String value = System.getProperty("jboss.qualified.host.name");
        if (value == null) {
            value = System.getProperty("jboss.host.name");
            if (value == null) {
                value = System.getProperty("jboss.node.name");
            }
        }
        return value == null ? "localhost" : formatPossibleIpv6Address(value);
    }

    public static int getHttpPort() {
        return Integer.parseInt(System.getProperty("jboss.http.port", "8080"));
    }

    public static String getHttpUrl() {
        return "http://" + getHost() + ":" + getHttpPort() + "/";
    }

    public static String formatPossibleIpv6Address(String address) {
        if (address == null) {
            return null;
        }
        if (!address.contains(":")) {
            return address;
        }
        if (address.startsWith("[") && address.endsWith("]")) {
            return address;
        }
        return "[" + address + "]";
    }
}
