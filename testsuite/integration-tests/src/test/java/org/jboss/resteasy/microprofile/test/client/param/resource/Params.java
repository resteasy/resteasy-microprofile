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

package org.jboss.resteasy.microprofile.test.client.param.resource;

import javax.ws.rs.PathParam;

import org.jboss.resteasy.annotations.jaxrs.QueryParam;

/**
 * Created by Marek Marusic <mmarusic@redhat.com> on 3/4/19.
 */
public class Params {
    @PathParam("p1")
    private String p1;

    @QueryParam("q1")
    private String q1;

    private String p3;

    public String getP1() {
        return p1;
    }

    public void setP1(String p1) {
        this.p1 = p1;
    }

    @PathParam("p3")
    public String getP3() {
        return p3;
    }

    @PathParam("p3")
    public void setP3(String p3) {
        this.p3 = p3;
    }

    public String getQ1() {
        return q1;
    }

    public void setQ1(String q1) {
        this.q1 = q1;
    }

}
