package org.jboss.resteasy.microprofile.client;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RestClientWithEmptyPathTest {
    @Test
    public void testBuildRestClient() {
        // the client get created without errors
        Assertions.assertDoesNotThrow(() -> RestClientBuilder.newBuilder()
                .baseUri("http://localhost")
                .build(RestClientWithEmptyPath.class));
    }
}
