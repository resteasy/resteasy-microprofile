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

import java.lang.reflect.ReflectPermission;
import java.util.PropertyPermission;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.resteasy.microprofile.config.ConfigConfiguration;
import org.jboss.resteasy.microprofile.test.config.resource.MicroProfileConfigResource;
import org.jboss.resteasy.microprofile.test.config.resource.TestConfigApplication;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.resteasy.spi.config.ConfigurationFactory;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

/**
 * Tests that the {@link org.jboss.resteasy.microprofile.config.ConfigConfigurationFactory} is the one used inside
 * the container.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
public class ConfigConfigurationFactoryTest {

    @Deployment
    public static WebArchive deployment() {
        return TestEnvironment.createWar(ConfigConfigurationFactoryTest.class)
                .addClasses(TestConfigApplication.class, MicroProfileConfigResource.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(DeploymentDescriptors.createPermissionsXmlAsset(
                        new PropertyPermission("arquillian.*", "read"),
                        new ReflectPermission("suppressAccessChecks"),
                        new RuntimePermission("accessDeclaredMembers")),
                        "permissions.xml");
    }

    @Test
    public void checkConfigurationFactory() {
        final ConfigurationFactory factory = ConfigurationFactory.getInstance();
        Assertions.assertTrue(factory.getConfiguration() instanceof ConfigConfiguration,
                String.format("Expected configuration %s to be an instance of ConfigConfiguration", factory));
    }
}
