/*
 * Copyright The RESTEasy Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.resteasy.microprofile.test.client.integration;

import java.lang.reflect.ReflectPermission;
import java.util.Map;
import java.util.PropertyPermission;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

/**
 * Verifies the local {@link ResponseExceptionMapper} contract of a provider registered by class is keyed by the
 * registered class, not by the CDI client proxy class of a normal-scoped provider.
 */
@ArquillianTest
public class ScopedProviderContractsTest {

    @Inject
    private ScopedMultiProvider injectedProvider;

    @Deployment
    public static WebArchive deployment() {
        return TestEnvironment.createWar(ScopedProviderContractsTest.class)
                .addClasses(ScopedMultiProvider.class)
                .addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(
                        new PropertyPermission("arquillian.*", "read"),
                        new ReflectPermission("suppressAccessChecks"),
                        new RuntimePermission("accessDeclaredMembers")),
                        "permissions.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void localContractKeyedByRegisteredClass() {
        // Guard: the provider must be a client proxy, otherwise this test does not exercise the proxy path
        Assertions.assertNotEquals(ScopedMultiProvider.class, injectedProvider.getClass(),
                "Expected a CDI client proxy for the @ApplicationScoped provider");

        final RestClientBuilder builder = RestClientBuilder.newBuilder()
                .register(ScopedMultiProvider.class);
        final Map<Class<?>, Integer> contracts = builder.getConfiguration().getContracts(ScopedMultiProvider.class);
        Assertions.assertTrue(contracts.containsKey(ResponseExceptionMapper.class),
                () -> String.format("Expected the ResponseExceptionMapper contract for %s but found %s",
                        ScopedMultiProvider.class.getName(), contracts));
    }

    @ApplicationScoped
    public static class ScopedMultiProvider implements ResponseExceptionMapper<RuntimeException>, ClientRequestFilter {

        @Override
        public RuntimeException toThrowable(final Response response) {
            return null;
        }

        @Override
        public void filter(final ClientRequestContext requestContext) {
        }
    }
}
