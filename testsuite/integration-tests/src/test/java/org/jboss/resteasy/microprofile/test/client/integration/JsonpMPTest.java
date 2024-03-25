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
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.microprofile.test.client.integration.resource.JsonpMPService;
import org.jboss.resteasy.microprofile.test.client.integration.resource.JsonpMPServiceIntf;
import org.jboss.resteasy.microprofile.test.util.TestEnvironment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @tpSubChapter MicroProfile rest client
 * @tpChapter Integration tests
 * @tpTestCaseDetails Show JSON-P is supported.
 * @tpSince RESTEasy 4.6.0
 */
@ExtendWith(ArquillianExtension.class)
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

    @BeforeEach
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
        Assertions.assertTrue(response.containsKey("name"),
                "JsonObject from the response doesn't contain field 'name'");
        Assertions.assertEquals(response.getJsonString("name").getString(), "Bill",
                "JsonObject from the response doesn't contain correct value for the field 'name'");
        Assertions.assertTrue(response.containsKey("id"),
                "JsonObject from the response doesn't contain field 'id'");
        Assertions.assertEquals(response.getJsonNumber("id").longValue(), 10001,
                "JsonObject from the response doesn't contain correct value for the field 'id'");
    }

    @Test
    public void testStructure() {
        JsonStructure structure = (JsonStructure) Json.createObjectBuilder().add("name", "Bill").build();
        JsonStructure response = jsonpMPServiceIntf.object(structure);
        JsonObject obj = (JsonObject) response;
        Assertions.assertTrue(obj.containsKey("name"),
                "JsonObject from the response doesn't contain field 'name'");
        Assertions.assertEquals(obj.getJsonString("name").getString(), "Bill",
                "JsonObject from the response doesn't contain correct value for the field 'name'");
    }

    @Test
    public void testJsonNumber() {
        JsonNumber jsonNumber = Json.createValue(100);
        JsonNumber response = jsonpMPServiceIntf.testNumber(jsonNumber);
        Assertions.assertTrue(response.intValue() == 200,
                "JsonNumber object with 200 value is expected");
    }

    @Test
    public void testArray() {
        JsonArray array = Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("name", "Bill").build())
                .add(Json.createObjectBuilder().add("name", "Monica").build())
                .build();

        JsonArray response = jsonpMPServiceIntf.array(array);
        Assertions.assertEquals(2, response.size(),
                "JsonArray from the response doesn't contain two elements as it should");
        JsonObject obj = response.getJsonObject(0);
        Assertions.assertTrue(obj.containsKey("name"),
                "JsonObject[0] from the response doesn't contain field 'name'");
        Assertions.assertEquals(obj.getJsonString("name").getString(), "Bill",
                "JsonObject[0] from the response doesn't contain correct value for the field 'name'");
        obj = response.getJsonObject(1);
        Assertions.assertTrue(obj.containsKey("name"),
                "JsonObject[1] from the response doesn't contain field 'name'");
        Assertions.assertEquals(obj.getJsonString("name").getString(), "Monica",
                "JsonObject[1] from the response doesn't contain correct value for the field 'name'");
    }

    @Test
    public void testJsonString() throws Exception {

        JsonString jsonString = Json.createValue("Resteasy");
        JsonString response = jsonpMPServiceIntf.testString(jsonString);

        Assertions.assertTrue(response.getString().equals("Hello Resteasy"),
                "JsonString object with Hello Resteasy value is expected");
    }
}
