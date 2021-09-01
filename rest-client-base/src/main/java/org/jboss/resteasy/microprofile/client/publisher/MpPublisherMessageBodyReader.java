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

package org.jboss.resteasy.microprofile.client.publisher;


import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.jboss.resteasy.plugins.providers.sse.SseConstants;
import org.jboss.resteasy.plugins.providers.sse.SseEventInputImpl;
import org.reactivestreams.Publisher;

@Provider
@Consumes(MediaType.SERVER_SENT_EVENTS)
public class MpPublisherMessageBodyReader implements MessageBodyReader<Publisher<?>> {
    @Context
    protected Providers providers;
    private final ExecutorService executor;

    public MpPublisherMessageBodyReader(final ExecutorService ex) {
        executor = ex;
    }

    public MpPublisherMessageBodyReader() {
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Publisher.class.isAssignableFrom(type) && MediaType.SERVER_SENT_EVENTS_TYPE.isCompatible(mediaType);
    }

    @Override
    public Publisher<?> readFrom(Class<Publisher<?>> type, Type genericType, Annotation[] annotations,
                                 MediaType mediaType,
                                 MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws WebApplicationException {
        MediaType streamType = mediaType;
        if (mediaType.getParameters() != null) {
            Map<String, String> map = mediaType.getParameters();
            String elementType = map.get(SseConstants.SSE_ELEMENT_MEDIA_TYPE);
            if (elementType != null) {
                streamType = MediaType.valueOf(elementType);
            }
        }
        SseEventInputImpl sseEventInput = new SseEventInputImpl(annotations, streamType, mediaType, httpHeaders, entityStream);
        return new SSEPublisher<>(genericType, providers, sseEventInput, executor);
    }
}
