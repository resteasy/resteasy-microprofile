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

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/")
public class OptionalConfigPropertyInjectionResource {

    public static final String MISSING_OPTIONAL_PROPERTY_PATH = "/misingOptionalProperty";
    public static final String PRESENT_OPTIONAL_PROPERTY_PATH = "/presentOptionalProperty";

    private static final String MISSING_OPTIONAL_PROPERTY_NAME = "org.jboss.resteasy.test.missingOptionalProperty";
    private static final String PRESENT_OPTIONAL_PROPERTY_NAME = "org.jboss.resteasy.test.presentOptionalProperty";
    public static final String OPTIONAL_PROPERTY_VALUE = "I'm a optional property but I'm here";

    static {
        System.setProperty(PRESENT_OPTIONAL_PROPERTY_NAME, OPTIONAL_PROPERTY_VALUE);
    }

    @Inject
    @ConfigProperty(name = MISSING_OPTIONAL_PROPERTY_NAME)
    private Optional<String> missingOptionalProperty;
    @Inject
    @ConfigProperty(name = PRESENT_OPTIONAL_PROPERTY_NAME)
    private Optional<String> presentOptionalProperty;

    @GET
    @Produces("text/plain")
    @Path(MISSING_OPTIONAL_PROPERTY_PATH)
    public String getMisingOptionalProperty() {
        return missingOptionalProperty.orElse(null);
    }

    @GET
    @Produces("text/plain")
    @Path(PRESENT_OPTIONAL_PROPERTY_PATH)
    public String getPresentOptionalProperty() {
        return presentOptionalProperty.orElseThrow(InternalServerErrorException::new);
    }

}
