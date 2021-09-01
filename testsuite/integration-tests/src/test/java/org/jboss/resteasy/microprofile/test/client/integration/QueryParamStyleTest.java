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

package org.jboss.resteasy.microprofile.test.client.integration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.ext.QueryParamStyle;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.QueryParamStyleService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.QueryParamStyleServiceIntf;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @tpSubChapter MicroProfile rest client
 * @tpChapter Integration tests
 * @tpTestCaseDetails Show QueryParamStyle working.
 * @tpSince RESTEasy 4.6.0
 */
@RunWith(Arquillian.class)
@RunAsClient
public class QueryParamStyleTest {

    @Deployment
    public static Archive<?> serviceDeploy() {
        return TestEnvironment.createWar(QueryParamStyleTest.class)
                .addClass(QueryParamStyleService.class);
    }

    private RestClientBuilder builder;
    private final List<String> argList = new ArrayList<>();

    @ArquillianResource
    private URL url;

    @Before
    public void before() throws Exception {
        builder = RestClientBuilder.newBuilder();
        builder.baseUri(TestEnvironment.generateUri(url, "test-app"));

        argList.clear();
        argList.add("client call");
        argList.add("hello");
        argList.add("three");
    }

    /*
     * Use default format setting (i.e. QueryParamStyle.MULTI_PAIRS)
     */
    @Test
    public void defaultSetting() {

        QueryParamStyleServiceIntf serviceIntf = builder
                .build(QueryParamStyleServiceIntf.class);
        List<String> l = serviceIntf.getList(argList);

        Assert.assertEquals(4, l.size());
        Assert.assertEquals("theService reached", l.get(3));
    }

    /*
     * Use QueryParamStyle.MULTI_PAIRS
     */
    @Test
    public void multiPairs() {

        QueryParamStyleServiceIntf serviceIntf = builder
                .queryParamStyle(QueryParamStyle.MULTI_PAIRS)
                .build(QueryParamStyleServiceIntf.class);

        List<String> l = serviceIntf.getList(argList);
        Assert.assertEquals(4, l.size());
        Assert.assertEquals("theService reached", l.get(3));
    }

    /*
     * Use QueryParamStyle.COMMA_SEPARATED
     */
    @Test
    public void commaSeparated() {

        QueryParamStyleServiceIntf serviceIntf = builder
                .queryParamStyle(QueryParamStyle.COMMA_SEPARATED)
                .build(QueryParamStyleServiceIntf.class);

        List<String> l = serviceIntf.getList(argList);
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("client call,hello,three", l.get(0));
    }

    /*
     * Use QueryParamStyle.ARRAY_PAIRS
     */
    @Test
    public void arraPairs() {

        QueryParamStyleServiceIntf serviceIntf = builder
                .queryParamStyle(QueryParamStyle.ARRAY_PAIRS)
                .build(QueryParamStyleServiceIntf.class);

        List<String> l = serviceIntf.getList(argList);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals("theService reached", l.get(0));
    }
}
