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

package org.jboss.resteasy.microprofile.test.client.deployments;

import java.io.IOException;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ArquillianTest
@RunAsClient
public class EarTwoWarsTest extends AbstractMultipleDeploymentsTest {

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() throws IOException {
        final Map<String, String> config = Map.of("dev.resteasy.test.deployment.name", "ear");
        return TestEnvironment.addConfigProperties(ShrinkWrap.create(EnterpriseArchive.class, "two-wars-test.ear")
                .addAsLibrary(createSharedJar())
                .addAsModule(createWar(DEPLOYMENT_1, DEPLOYMENT_2))
                .addAsModule(createWar(DEPLOYMENT_2, DEPLOYMENT_1)), config);
    }
}
