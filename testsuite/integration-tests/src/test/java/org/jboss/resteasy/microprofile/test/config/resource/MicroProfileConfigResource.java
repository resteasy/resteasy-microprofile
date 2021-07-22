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

package org.jboss.resteasy.microprofile.test.config.resource;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

@Path("/")
public class MicroProfileConfigResource {

    static {
        System.setProperty("system", "system-system");
    }

    @Inject
    Config config;

    @GET
    @Produces("text/plain")
    @Path("system/prog")
    public String systemProg() {
        return ConfigProvider.getConfig().getOptionalValue("system", String.class).orElse("d'oh");
    }

    @GET
    @Produces("text/plain")
    @Path("system/inject")
    public String systemInject() {
        return config.getOptionalValue("system", String.class).orElse("d'oh");
    }

    @GET
    @Produces("text/plain")
    @Path("init/prog")
    public String initProg() {
        return ConfigProvider.getConfig().getOptionalValue("init", String.class).orElse("d'oh");
    }

    @GET
    @Produces("text/plain")
    @Path("init/inject")
    public String initInject() {
        return config.getOptionalValue("init", String.class).orElse("d'oh");
    }

    @GET
    @Produces("text/plain")
    @Path("filter/prog")
    public String filterProg() {
        return ConfigProvider.getConfig().getOptionalValue("filter", String.class).orElse("d'oh");
    }

    @GET
    @Produces("text/plain")
    @Path("filter/inject")
    public String filterInject() {
        return config.getOptionalValue("filter", String.class).orElse("d'oh");
    }

    @GET
    @Produces("text/plain")
    @Path("context/prog")
    public String contextProg() {
        return ConfigProvider.getConfig().getOptionalValue("context", String.class).orElse("d'oh");
    }

    @GET
    @Produces("text/plain")
    @Path("context/inject")
    public String contextInject() {
        return config.getOptionalValue("context", String.class).orElse("d'oh");
    }

    @GET
    @Produces("text/plain")
    @Path("actual")
    public String testActualContextParameter() {
        return "actual";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("configSources/ordinal")
    public Map<String, Integer> getConfigSources(@QueryParam("inject") boolean inject) {
        Iterable<ConfigSource> configSources;
        if (inject) {
            configSources = config.getConfigSources();
        } else {
            configSources = ConfigProvider.getConfig().getConfigSources();
        }
        Map<String, Integer> ordinalByConfigSource = new HashMap<>();
        for (ConfigSource configSource : configSources) {

            ordinalByConfigSource.put(configSource.getClass().getCanonicalName(), configSource.getOrdinal());
        }
        return ordinalByConfigSource;
    }

}