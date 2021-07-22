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

package org.jboss.resteasy.microprofile.test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.client.helpers.Operations.CompositeOperationBuilder;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LoggingServerSetupTask implements ServerSetupTask {

    private final Deque<ModelNode> cleanupOps = new ArrayDeque<>();

    @Override
    public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
        final CompositeOperationBuilder builder = CompositeOperationBuilder.create();

        for (Map.Entry<String, String> entry : getLoggers().entrySet()) {
            final ModelNode loggerAddress = Operations.createAddress("subsystem", "logging", "logger", entry.getKey());
            final ModelNode op = Operations.createAddOperation(loggerAddress);
            op.get("level").set(entry.getValue());
            builder.addStep(op);
            cleanupOps.addFirst(Operations.createRemoveOperation(loggerAddress));
        }

        final ModelNode consoleAddress = Operations.createAddress("subsystem", "logging", "console-handler", "CONSOLE");
        builder.addStep(Operations.createWriteAttributeOperation(consoleAddress, "level", "ALL"));
        cleanupOps.addLast(Operations.createWriteAttributeOperation(consoleAddress, "level", "INFO"));
        executeOperation(managementClient.getControllerClient(), builder.build());
    }

    @Override
    public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
        final CompositeOperationBuilder builder = CompositeOperationBuilder.create();
        for (ModelNode op : cleanupOps) {
            builder.addStep(op);
        }
        executeOperation(managementClient.getControllerClient(), builder.build());
    }

    /**
     * A map where the key is the logger name and the value is the log level to assign to the logger.
     *
     * @return a map of loggers and log levels
     */
    protected Map<String, String> getLoggers() {
        return Collections.singletonMap("org.jboss.resteasy", "DEBUG");
    }

    private void executeOperation(final ModelControllerClient client, final Operation op) throws IOException {
        final ModelNode result = client.execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            throw new RuntimeException(Operations.getFailureDescription(result).asString());
        }
    }
}
