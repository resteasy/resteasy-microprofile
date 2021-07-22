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

package org.jboss.resteasy.microprofile.test.client.integration.resource;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.junit.Assert;

@Path("/jsonpService")
public class JsonpMPService {
    @Path("array")
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public JsonArray array(JsonArray array) {
        Assert.assertEquals("The request didn't contain 2 json elements", 2, array.size());
        JsonObject obj = array.getJsonObject(0);
        Assert.assertTrue("The field 'name' didn't propagated correctly from the request for object[0]",
                obj.containsKey("name"));
        Assert.assertEquals("The value of field 'name' didn't propagated correctly from the request for object[0]",
                obj.getJsonString("name").getString(), "Bill");
        obj = array.getJsonObject(1);
        Assert.assertTrue("The field 'name' didn't propagated correctly from the request for object[1]",
                obj.containsKey("name"));
        Assert.assertEquals("The value of field 'name' didn't propagated correctly from the request for object[1]",
                obj.getJsonString("name").getString(), "Monica");
        return array;
    }

    @Path("object")
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public JsonObject object(JsonObject obj) {
        Assert.assertTrue("The field 'name' didn't propagated correctly from the request", obj.containsKey("name"));
        Assert.assertEquals("The value of field 'name' didn't propagated correctly from the request"
                , obj.getJsonString("name").getString(), "Bill");
        if (obj.containsKey("id")) {
            Assert.assertEquals("The value of field 'id' didn't propagated correctly from the request"
                    , obj.getJsonNumber("id").longValue(), 10001);
        }
        return obj;
    }

    @Path("structure")
    @POST
    @Produces("application/json")
    @Consumes("application/json")
    public JsonStructure object(JsonStructure struct) {
        JsonObject obj = (JsonObject) struct;
        Assert.assertTrue("The field 'name' didn't propagated correctly from the request", obj.containsKey("name"));
        Assert.assertEquals("The value of field 'name' didn't propagated correctly from the request",
                obj.getJsonString("name").getString(), "Bill");
        return obj;
    }


    @Path("number")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNumber testNumber(JsonNumber number) {
        return Json.createValue(number.intValue() + 100);
    }

    @Path("string")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonString testString(JsonString string) {
        return Json.createValue("Hello " + string.getString());
    }

}
