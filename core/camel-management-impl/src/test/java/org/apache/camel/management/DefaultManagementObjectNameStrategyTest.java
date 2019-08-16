/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.management;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.engine.DefaultCamelContextNameStrategy;
import org.apache.camel.spi.ManagementObjectNameStrategy;
import org.junit.Before;
import org.junit.Test;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static org.junit.Assert.assertEquals;

/**
 * Tests proper behavior of DefaultManagementObjectNameStrategy when
 * required to create an {@link ObjectName}
 */
public class DefaultManagementObjectNameStrategyTest {

    private CamelContext context;
    private ManagementObjectNameStrategy managementObjectNameStrategy;

    @Before
    public void setCamelContext() {
        context = new DefaultCamelContext();
        managementObjectNameStrategy = new DefaultManagementObjectNameStrategy();
        // reset the counter for every test case
        DefaultCamelContextNameStrategy.setCounter(0);
    }

    @Test
    public void testObjectNameDefaultCamelContext() throws MalformedObjectNameException {
        ObjectName on = managementObjectNameStrategy.getObjectNameForCamelContext(context);

        assertEquals("org.apache.camel", on.getDomain());
        assertEquals("\"camel-1\"", on.getKeyProperty(DefaultManagementObjectNameStrategy.KEY_CONTEXT));
        assertEquals("context", on.getKeyProperty(DefaultManagementObjectNameStrategy.KEY_TYPE));
        assertEquals("\"camel-1\"", on.getKeyProperty(DefaultManagementObjectNameStrategy.KEY_NAME));
    }

    @Test
    public void testObjectNameCommaCamelContext() throws MalformedObjectNameException {
        context.setManagementName("test,comma");

        ObjectName on = managementObjectNameStrategy.getObjectNameForCamelContext(context);

        assertEquals("org.apache.camel", on.getDomain());
        assertEquals("\"test,comma\"", on.getKeyProperty(DefaultManagementObjectNameStrategy.KEY_CONTEXT));
        assertEquals("context", on.getKeyProperty(DefaultManagementObjectNameStrategy.KEY_TYPE));
        assertEquals("\"camel-1\"", on.getKeyProperty(DefaultManagementObjectNameStrategy.KEY_NAME));
    }

}
