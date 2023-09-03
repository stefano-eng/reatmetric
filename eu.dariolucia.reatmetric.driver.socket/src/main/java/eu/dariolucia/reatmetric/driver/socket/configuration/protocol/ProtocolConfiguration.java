/*
 * Copyright (c)  2023 Dario Lucia (https://www.dariolucia.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package eu.dariolucia.reatmetric.driver.socket.configuration.protocol;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.LinkedList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProtocolConfiguration {

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "entity-offset")
    private int entityOffset = 0;

    @XmlElement(name = "route")
    private List<RouteConfiguration> routes = new LinkedList<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEntityOffset() {
        return entityOffset;
    }

    public void setEntityOffset(int entityOffset) {
        this.entityOffset = entityOffset;
    }

    public List<RouteConfiguration> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteConfiguration> routes) {
        this.routes = routes;
    }

    /* ***************************************************************
     * Internal operations
     * ***************************************************************/

    public void initialise() {
        for(RouteConfiguration r : getRoutes()) {
            r.initialise(this);
        }
    }
}