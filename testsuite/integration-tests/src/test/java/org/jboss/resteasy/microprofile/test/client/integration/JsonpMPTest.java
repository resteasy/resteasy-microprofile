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

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.JsonpMPService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.JsonpMPServiceIntf;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @tpSubChapter MicroProfile rest client
 * @tpChapter Integration tests
 * @tpTestCaseDetails Show JSON-P is supported.
 * @tpSince RESTEasy 4.6.0
 */
@RunWith(Arquillian.class)
@RunAsClient
public class JsonpMPTest {

    @Deployment
    public static Archive<?> serviceDeploy() {
        return TestEnvironment.createWar(JsonpMPTest.class)
                .addClass(JsonpMPService.class);
    }

    private JsonpMPServiceIntf jsonpMPServiceIntf;

    @ArquillianResource
    private URL url;

    @Before
    public void before() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        jsonpMPServiceIntf = builder
                .baseUri(TestEnvironment.generateUri(url, "test-app"))
                .build(JsonpMPServiceIntf.class);
    }

    @Test
    public void testObject() {

        JsonObject obj = Json.createObjectBuilder()
                .add("name", "Bill")
                .add("id", 10001)
                .build();

        JsonObject response = jsonpMPServiceIntf.object(obj);
        Assert.assertTrue("JsonObject from the response doesn't contain field 'name'",
                response.containsKey("name"));
        Assert.assertEquals("JsonObject from the response doesn't contain correct value for the field 'name'",
                response.getJsonString("name").getString(), "Bill");
        Assert.assertTrue("JsonObject from the response doesn't contain field 'id'",
                response.containsKey("id"));
        Assert.assertEquals("JsonObject from the response doesn't contain correct value for the field 'id'",
                response.getJsonNumber("id").longValue(), 10001);
    }

    @Test
    public void testStructure() {
        JsonStructure structure = (JsonStructure) Json.createObjectBuilder().add("name", "Bill").build();
        JsonStructure response = jsonpMPServiceIntf.object(structure);
        JsonObject obj = (JsonObject) response;
        Assert.assertTrue("JsonObject from the response doesn't contain field 'name'",
                obj.containsKey("name"));
        Assert.assertEquals("JsonObject from the response doesn't contain correct value for the field 'name'",
                obj.getJsonString("name").getString(), "Bill");
    }

    @Test
    public void testJsonNumber() {
        JsonNumber jsonNumber = Json.createValue(100);
        JsonNumber response = jsonpMPServiceIntf.testNumber(jsonNumber);
        Assert.assertTrue("JsonNumber object with 200 value is expected",
                response.intValue() == 200);
    }

    @Test
    public void testArray() {
        JsonArray array = Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("name", "Bill").build())
                .add(Json.createObjectBuilder().add("name", "Monica").build())
                .build();

        JsonArray response = jsonpMPServiceIntf.array(array);
        Assert.assertEquals("JsonArray from the response doesn't contain two elements as it should",
                2, response.size());
        JsonObject obj = response.getJsonObject(0);
        Assert.assertTrue("JsonObject[0] from the response doesn't contain field 'name'",
                obj.containsKey("name"));
        Assert.assertEquals("JsonObject[0] from the response doesn't contain correct value for the field 'name'",
                obj.getJsonString("name").getString(), "Bill");
        obj = response.getJsonObject(1);
        Assert.assertTrue("JsonObject[1] from the response doesn't contain field 'name'",
                obj.containsKey("name"));
        Assert.assertEquals("JsonObject[1] from the response doesn't contain correct value for the field 'name'",
                obj.getJsonString("name").getString(), "Monica");
    }

    @Test
    public void testJsonString() throws Exception {

        JsonString jsonString = Json.createValue("Resteasy");
        JsonString response = jsonpMPServiceIntf.testString(jsonString);

        Assert.assertTrue("JsonString object with Hello Resteasy value is expected",
                response.getString().equals("Hello Resteasy"));
    }
}
