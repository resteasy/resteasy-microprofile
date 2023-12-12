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

package org.jboss.resteasy.microprofile.test.client.integration;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * MicroProfile rest client
 * Integration tests
 * Testing that using nonProxyHosts property with * near index 0 doesn't throw
 * regex.PatternSyntaxException while trying to build a rest client
 * https://github.com/resteasy/resteasy-microprofile/issues/10
 */
@RunWith(Arquillian.class)
@RunAsClient
public class RestClientNonProxyHostsPatternTest {

    //asterisk must be at index 0
    private static final String NON_PROXY_HOSTS = "*.localhost";

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> deploy() {
        return TestEnvironment.createWar(RestClientNonProxyHostsPatternTest.class)
                .addClasses(HelloClient.class);
    }

    @Test
    public void testProxyOperationAfterNonProxyHostSetWithFirstIndexAsterisk() {

        Map<String, String> originalProperties = new HashMap<>();
        originalProperties.put("http.proxyHost", System.getProperty("http.proxyHost"));
        originalProperties.put("http.proxyPort", System.getProperty("http.proxyPort"));
        originalProperties.put("http.nonProxyHosts", System.getProperty("http.nonProxyHosts"));

        try {
            System.setProperty("http.proxyHost", url.getHost());
            System.setProperty("http.proxyPort", String.valueOf(url.getPort()));
            System.setProperty("http.nonProxyHosts", NON_PROXY_HOSTS);

            HelloClient helloClient = RestClientBuilder.newBuilder().baseUrl(url).build(HelloClient.class);
            assertNotNull(helloClient);
        } finally {
            resetOriginalProperties(originalProperties);
        }
    }

    @RegisterRestClient
    public interface HelloClient {
    }

    private static void resetOriginalProperties(Map<String, String> originalProperties) {
        for (Map.Entry<String, String> property : originalProperties.entrySet()) {
            if (property.getValue() != null) {
                System.setProperty(property.getKey(), property.getValue());
            } else {
                System.clearProperty(property.getKey());
            }
        }
    }

}
