package org.jboss.resteasy.microprofile.client.http.engine;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import org.eclipse.microprofile.config.spi.Converter;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.engines.vertx.VertxClientHttpEngine;

// todo: we need to put this into a submodule instead of putting in test.
public class MPClientHttpEngineConverter implements Converter<ClientHttpEngine> {

    @Override
    public ClientHttpEngine convert(String engine) throws IllegalArgumentException, NullPointerException {
        if ("vertx.http2".equals(engine)) {

            Vertx vertx = Vertx.vertx();

            HttpClientOptions options = new HttpClientOptions();
            options.setSsl(true);
            options.setProtocolVersion(HttpVersion.HTTP_2);
            options.setUseAlpn(true);

            return new VertxClientHttpEngine(vertx, options);
        }

        throw new IllegalArgumentException("Unsupported HTTP Engine!");
    }

}

