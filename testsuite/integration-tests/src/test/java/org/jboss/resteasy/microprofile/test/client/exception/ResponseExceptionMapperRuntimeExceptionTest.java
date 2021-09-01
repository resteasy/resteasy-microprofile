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

package org.jboss.resteasy.microprofile.test.client.exception;

import static org.junit.Assert.fail;

import java.net.URL;
import javax.ws.rs.WebApplicationException;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ExceptionMapperRuntimeExceptionWithReasonMapper;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ResponseExceptionMapperRuntimeExceptionMapper;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ResponseExceptionMapperRuntimeExceptionResource;
import org.jboss.resteasy.microprofile.test.client.exception.resource.ResponseExceptionMapperRuntimeExceptionResourceInterface;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @tpSubChapter Resteasy-client
 * @tpChapter Integration tests
 * @tpSince RESTEasy 3.6.0
 * @tpTestCaseDetails Regression test for RESTEASY-1847
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ResponseExceptionMapperRuntimeExceptionTest {

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> createTestArchive() {
        return TestEnvironment.createWar(ResponseExceptionMapperRuntimeExceptionTest.class)
                .addClasses(ResponseExceptionMapperRuntimeExceptionMapper.class,
                        ExceptionMapperRuntimeExceptionWithReasonMapper.class,
                        ResponseExceptionMapperRuntimeExceptionResource.class,
                        ResponseExceptionMapperRuntimeExceptionResourceInterface.class,
                        ResponseExceptionMapper.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * @tpTestDetails Check ExceptionMapper for WebApplicationException
     * @tpSince RESTEasy 3.6.0
     */
    @Test
    public void testRuntimeApplicationException() throws Exception {
        ResponseExceptionMapperRuntimeExceptionResourceInterface service = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "test-app", "test",
                        ResponseExceptionMapperRuntimeExceptionTest.class.getSimpleName()))
                .register(ResponseExceptionMapperRuntimeExceptionMapper.class)
                .build(ResponseExceptionMapperRuntimeExceptionResourceInterface.class);
        try {
            service.get();
            fail("Should not get here");
        } catch (RuntimeException e) {
            // assert test exception message
            Assert.assertEquals(ExceptionMapperRuntimeExceptionWithReasonMapper.REASON, e.getMessage());
        }
    }

    @Test
    public void testRuntimeAException() throws Exception {
        ResponseExceptionMapperRuntimeExceptionResourceInterface service = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "test-app", "test",
                        ResponseExceptionMapperRuntimeExceptionTest.class.getSimpleName()))
                .build(ResponseExceptionMapperRuntimeExceptionResourceInterface.class);
        try {
            service.get();
            fail("Should not get here");
        } catch (WebApplicationException e) {
            String str = e.getResponse().readEntity(String.class);
            Assert.assertEquals("Test error occurred", str);
        }
    }


}
