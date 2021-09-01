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

package org.jboss.resteasy.microprofile.client;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.LocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.providers.DefaultTextPlain;
import org.jboss.resteasy.plugins.providers.IIOImageProvider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Assert;
import org.junit.Test;

public class ProviderFactoryTest {

    @Test
    public void testDefaultProvider() {
        RestClientBuilderImpl builder = (RestClientBuilderImpl) RestClientBuilder.newBuilder();

        Assert.assertTrue(builder.getBuilderDelegate()
                .getProviderFactory()
                .getProviderClasses()
                .contains(IIOImageProvider.class));
        Assert.assertTrue(builder.getBuilderDelegate()
                .getProviderFactory()
                .getProviderClasses()
                .contains(DefaultTextPlain.class));
    }

    @Test
    public void testCustomProvider() {
        try {
            ResteasyProviderFactory provider = new LocalResteasyProviderFactory();
            provider.registerProvider(IIOImageProvider.class);
            RestClientBuilderImpl.setProviderFactory(provider);

            RestClientBuilderImpl builder = (RestClientBuilderImpl) RestClientBuilder.newBuilder();

            Assert.assertTrue(builder.getBuilderDelegate()
                    .getProviderFactory()
                    .getProviderClasses()
                    .contains(IIOImageProvider.class));
            Assert.assertFalse(builder.getBuilderDelegate()
                    .getProviderFactory()
                    .getProviderClasses()
                    .contains(DefaultTextPlain.class));
        } finally {
            // Reset to default state
            RestClientBuilderImpl.setProviderFactory(null);
        }
    }
}
