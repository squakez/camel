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
package org.apache.camel.component.dataset;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.Registry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileDataSetConsumerTest extends ContextTestSupport {

    protected FileDataSet dataSet;

    final String testDataFileName = "src/test/data/file-dataset-test.txt";

    final String resultUri = "mock://result";
    final String dataSetName = "foo";
    final String dataSetUri = "dataset://" + dataSetName;

    @Override
    protected Registry createCamelRegistry() throws Exception {
        Registry answer = super.createCamelRegistry();
        answer.bind("foo", dataSet);
        return answer;
    }

    @Test
    public void testDefaultListDataSet() throws Exception {
        MockEndpoint result = getMockEndpoint(resultUri);
        result.expectedMinimumMessageCount((int) dataSet.getSize());

        result.assertIsSatisfied();
    }

    @Test
    public void testDefaultListDataSetWithSizeGreaterThanListSize() throws Exception {
        MockEndpoint result = getMockEndpoint(resultUri);
        dataSet.setSize(20);
        result.expectedMinimumMessageCount((int) dataSet.getSize());

        result.assertIsSatisfied();
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        dataSet = new FileDataSet(testDataFileName);
        assertEquals(1, dataSet.getSize(), "Unexpected DataSet size");
        super.setUp();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from(dataSetUri).to("mock://result");
            }
        };
    }
}
