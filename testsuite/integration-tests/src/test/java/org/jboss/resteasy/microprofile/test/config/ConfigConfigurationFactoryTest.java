/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2022 Red Hat, Inc., and individual contributors
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

package org.jboss.resteasy.microprofile.test.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.microprofile.config.ConfigConfigurationFactory;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.resteasy.spi.config.ConfigurationFactory;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that the {@link org.jboss.resteasy.microprofile.config.ConfigConfigurationFactory} is the one used inside
 * the container.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
public class ConfigConfigurationFactoryTest {

    @Deployment
    public static WebArchive deployment() {
        return TestEnvironment.createWar(ConfigConfigurationFactoryTest.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void checkConfigurationFactory() {
        final ConfigurationFactory factory = ConfigurationFactory.getInstance();
        Assert.assertTrue(String.format("Expected factory %s to be an instance of ConfigConfigurationFactory", factory),
                factory instanceof ConfigConfigurationFactory);
    }
}
