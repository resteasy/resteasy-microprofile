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

import java.util.function.Function;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.RedirectionException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.jboss.resteasy.client.exception.WebApplicationExceptionWrapper;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class DefaultResponseExceptionMapper implements ResponseExceptionMapper {

    private final boolean originalBehavior;
    private final boolean unwrappedExceptions;
    private final boolean serverSide;

    public DefaultResponseExceptionMapper() {
        final Config config = ConfigProvider.getConfig();
        originalBehavior = config
                .getOptionalValue(ResteasyContextParameters.RESTEASY_ORIGINAL_WEBAPPLICATIONEXCEPTION_BEHAVIOR, boolean.class)
                .orElse(false);

        serverSide = ResteasyDeployment.onServer();

        unwrappedExceptions = config
                .getOptionalValue(ResteasyContextParameters.RESTEASY_UNWRAPPED_EXCEPTIONS, boolean.class)
                .orElse(false);
    }

    private static Function<Response, ServerErrorException> serverExceptionConstructor(int status) {
        switch (status) {
            case 500:
                return InternalServerErrorException::new;
            case 503:
                return ServiceUnavailableException::new;
            default:
                return ServerErrorException::new;
        }
    }

    private static Function<Response, ClientErrorException> clientExceptionConstructor(int status) {
        switch (status) {
            case 400:
                return BadRequestException::new;
            case 401:
                return NotAuthorizedException::new;
            case 403:
                return ForbiddenException::new;
            case 404:
                return NotFoundException::new;
            case 405:
                return NotAllowedException::new;
            case 406:
                return NotAcceptableException::new;
            case 415:
                return NotSupportedException::new;
            default:
                return ClientErrorException::new;
        }
    }

    private static Function<Response, ? extends WebApplicationException> webApplicationException(
            int status) {

        if (status < 600) {
            if (status >= 500) {
                return serverExceptionConstructor(status);
            }
            if (status >= 400) {
                return clientExceptionConstructor(status);
            }
            if (status >= 300) {
                return RedirectionException::new;
            }
        }
        return WebApplicationException::new;
    }

    @Override
    public Throwable toThrowable(Response response) {
        try {
            response.bufferEntity();
        } catch (Exception ignored) {
        }

        WebApplicationException unwrapped = webApplicationException(response.getStatus()).apply(response);
        return unwrappedExceptions ? unwrapped : WebApplicationExceptionWrapper.wrap(unwrapped);
    }

    @Override
    public boolean handles(int status, MultivaluedMap headers) {
        return status >= (originalBehavior || !serverSide ? 400 : 300);
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}
