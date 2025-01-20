/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2025 Red Hat, Inc., and individual contributors
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

package dev.resteasy.microprofile.rest.client.tck;

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.net.SocketPermission;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyPermission;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ManifestContainer;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class RestClientTckApplicationArchiveProcessor implements ApplicationArchiveProcessor {
    private static final List<String> ALL_FILE_TESTS = List.of(
            // Creates a /tmp/ssl* keystore file
            "org.eclipse.microprofile.rest.client.tck.ssl.SslContextTest",
            "org.eclipse.microprofile.rest.client.tck.ssl.SslHostnameVerifierTest",
            "org.eclipse.microprofile.rest.client.tck.ssl.SslMutualTest",
            "org.eclipse.microprofile.rest.client.tck.ssl.SslTrustStoreTest");

    private static final List<String> GET_CLASS_LOADER_TESTS = List.of(
            // The org.jboss.arquillian.testenricher.cdi.CDIInjectionEnricher requires getClassLoader()
            "org.eclipse.microprofile.rest.client.tck.cditests.CDIProxyServerTest",
            "org.eclipse.microprofile.rest.client.tck.ssl.SslHostnameVerifierTest",
            "org.eclipse.microprofile.rest.client.tck.ssl.SslMutualTest",
            "org.eclipse.microprofile.rest.client.tck.ssl.SslTrustStoreTest",
            // Required for the Jetty thread pool
            "org.eclipse.microprofile.rest.client.tck.ProxyServerTest");

    private static final List<String> JETTY_SERVER_TESTS = List.of(
            "org.eclipse.microprofile.rest.client.tck.ProxyServerTest",
            "org.eclipse.microprofile.rest.client.tck.cditests.CDIProxyServerTest");

    @Override
    public void process(final Archive<?> applicationArchive, final TestClass testClass) {
        if (applicationArchive instanceof ManifestContainer<?>) {
            final var container = (ManifestContainer<?>) applicationArchive;

            final var permissions = new ArrayList<>(List.of(
                    // Required by the Arquillian ServiceLoader to allow access to the constructor
                    new ReflectPermission("suppressAccessChecks"),
                    new PropertyPermission("arquillian.*", "read"),
                    // Required by Jetty
                    new PropertyPermission("jetty.*", "read,write"),
                    // Required by the MP REST Client TCK
                    new PropertyPermission("org.eclipse.microprofile.rest.client.*", "read"),
                    new RuntimePermission("getenv.JETTY_AVAILABLE_PROCESSORS"),
                    // Required of OTel is available on the deployment class path
                    new RuntimePermission("getenv.OTEL_JAVAAGENT_DEBUG"),
                    new RuntimePermission("getenv.OTEL_INSTRUMENTATION_EXPERIMENTAL_SPAN_SUPPRESSION_STRATEGY"),
                    // Required by TestNG
                    new PropertyPermission("testng.*", "read"),
                    new PropertyPermission("user.dir", "read"),
                    new RuntimePermission("accessDeclaredMembers"),
                    // Required by Wiremock
                    new PropertyPermission("wiremock.*", "read"),
                    new SocketPermission("localhost", "resolve"),
                    new SocketPermission("localhost:*", "connect,listen,resolve"),
                    new SocketPermission("127.0.0.1:*", "connect,resolve")));

            if (ALL_FILE_TESTS.contains(testClass.getName())) {
                permissions.add(new FilePermission("<<ALL FILES>>", "read"));
            }
            if (GET_CLASS_LOADER_TESTS.contains(testClass.getName())) {
                permissions.add(new RuntimePermission("getClassLoader"));
            }
            if (JETTY_SERVER_TESTS.contains(testClass.getName())) {
                permissions.add(new RuntimePermission("modifyThread"));
            }

            var currentPermissionsXml = applicationArchive.delete("META-INF/permissions.xml");
            // A WAR might be in a different location
            if (currentPermissionsXml == null) {
                currentPermissionsXml = applicationArchive.delete("WEB-INF/classes/META-INF/permissions.yaml");
            }
            if (currentPermissionsXml != null) {
                container.addAsManifestResource(
                        DeploymentDescriptors.appendPermissions(currentPermissionsXml.getAsset(), permissions),
                        "permissions.xml");
            } else {
                container.addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(permissions),
                        "permissions.xml");
            }
        }
    }
}
