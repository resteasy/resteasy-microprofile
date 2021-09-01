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

package org.jboss.resteasy.microprofile.test.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.resteasy.microprofile.test.LoggingServerSetupTask;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class CustomLoggingServerSetupTask extends LoggingServerSetupTask {
    @Override
    protected Map<String, String> getLoggers() {
        final Map<String, String> result = new LinkedHashMap<>();
        result.put("org.jboss.as.jaxrs", "TRACE");
        result.put("org.jboss.resteasy", "TRACE");
        return result;
    }
}
