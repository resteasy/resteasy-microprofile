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

package org.jboss.resteasy.context;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

import org.eclipse.microprofile.context.spi.ContextManagerProvider;
import org.jboss.resteasy.core.ResteasyContext;
import org.jboss.resteasy.spi.Dispatcher;

@Provider
public class ContextFeature implements Feature {
    @Override
    public boolean configure(FeatureContext context) {
        // this is tied to the deployment, which is what we want for the reactive context
        if (context.getConfiguration().getRuntimeType() == RuntimeType.CLIENT)
            return false;
        Dispatcher dispatcher = ResteasyContext.getContextData(Dispatcher.class);
        if (dispatcher == null) {
            // this can happen, but it means we're not able to find a deployment
            return false;
        }
        // Make sure we have context propagation for this class loader
        ContextManagerProvider.instance().getContextManager();
        return true;
    }

}
