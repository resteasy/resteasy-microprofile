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

package org.jboss.resteasy.microprofile.test.client.param;

import java.net.URL;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.param.resource.Params;
import org.jboss.resteasy.microprofile.test.client.param.resource.ProxyBeanParam;
import org.jboss.resteasy.microprofile.test.client.param.resource.ProxyBeanParamResource;
import org.jboss.resteasy.microprofile.test.client.param.resource.ProxyParameterAnnotations;
import org.jboss.resteasy.microprofile.test.client.param.resource.ProxyParameterAnotationsResource;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @tpSubChapter Parameters
 * @tpChapter Integration tests
 * @tpTestCaseDetails Test for RESTEasy param annotations (https://issues.jboss.org/browse/RESTEASY-1880)
 *                    Test logic is in the end-point in deployment.
 * @tpSince RESTEasy 3.6
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class RESTEasyParamBasicTest {

    @ArquillianResource
    private URL url;

    @Deployment
    public static Archive<?> deploySimpleResource() {
        return TestEnvironment.createWar(RESTEasyParamBasicTest.class)
                .addClasses(
                        ProxyBeanParamResource.class,
                        Params.class,
                        ProxyParameterAnotationsResource.class);
    }

    /**
     * @tpTestDetails Basic check of new query parameters, matrix parameters, header parameters, cookie parameters and form
     *                parameters in proxy clients
     *                Test checks that RESTEasy can inject correct values to method attributes in proxy clients
     *                This test uses new annotations without any annotation value in the proxy, however the annotation values
     *                are used in the server side resource.
     * @tpSince RESTEasy 4.0
     */
    @Test
    public void proxyClientAllParamsTest() throws Exception {
        //AllAtOnce with MicroProfile client
        ProxyParameterAnnotations mpRestClient = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .build(ProxyParameterAnnotations.class);
        final String response = mpRestClient.executeAllParams(
                "queryParam0",
                "headerParam0",
                "cookieParam0",
                "pathParam0",
                "formParam0",
                "matrixParam0");
        Assertions.assertEquals("queryParam0 headerParam0 cookieParam0 pathParam0 formParam0 matrixParam0", response);
    }

    /**
     * @tpTestDetails Basic check of the Resteasy Microprofile client with BeanParam.
     *                Test checks that Resteasy Microprofile client can inject correct values when BeanParam is used.
     *                This test uses new annotation only without any annotation value.
     * @tpSince RESTEasy 3.7
     */
    @Test
    public void testMicroprofileBeanParam() throws Exception {
        Params params = new Params();
        params.setP1("test");
        params.setP3("param3");
        params.setQ1("queryParam");

        ProxyBeanParam mpRestClient = RestClientBuilder.newBuilder()
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .build(ProxyBeanParam.class);
        final String response = mpRestClient.getAll(params, "param2", "queryParam1");
        Assertions.assertEquals("test_param2_param3_queryParam_queryParam1", response);
    }

}
