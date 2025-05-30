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
package org.apache.camel.dsl.jbang.it;

import org.apache.camel.dsl.jbang.it.support.JBangTestSupport;
import org.junit.jupiter.api.Test;

public class CamelCatalogITCase extends JBangTestSupport {

    private static final String COMPONENT_REGEX
            = "^\\sNAME\\s+LEVEL\\s+SINCE\\s+DESCRIPTION\\s+\\n(\\s([a-z0-9]-?)+\\s*(Stable|Preview|Experimental)\\s*[1-9]+.[0-9]+\\s{2}(.*\\n.*)+\\n)+";
    private static final String KAMELET_REGEX
            = "^\\sNAME\\s+TYPE\\s+LEVEL\\s+DESCRIPTION\\s+\\n(\\s([a-z0-9]+-?)+\\s+(action|sink|source)\\s+(Stable|Preview|Experimental)\\s+(.*\\n)+)+";
    private static final String TRANSFORMER_REGEX
            = "^\\sNAME\\s+LEVEL\\s+SINCE\\s+DESCRIPTION\\s+\\n(\\s([a-z0-9]-?)+\\s*(.*\\n.*)+\\n)+";
    private static final String DEV_CONSOLE_REGEX
            = "^\\sNAME\\s+LEVEL\\s+SINCE\\s+DESCRIPTION\\s+\\n(\\s([a-z0-9]-?)+\\s*(Stable|Preview|Experimental)(.*\\n.*)+\\n)+";

    @Test
    public void testCatalogComponents() {
        checkCommandOutputsPattern("catalog component", COMPONENT_REGEX);
    }

    @Test
    public void testCatalogKamelets() {
        checkCommandOutputsPattern("catalog kamelet", KAMELET_REGEX);
    }

    @Test
    public void testCatalogDataformats() {
        checkCommandOutputsPattern("catalog dataformat", COMPONENT_REGEX);
    }

    @Test
    public void testCatalogLanguages() {
        checkCommandOutputsPattern("catalog language", COMPONENT_REGEX);
    }

    @Test
    public void testCatalogTransformers() {
        checkCommandOutputsPattern("catalog transformer", TRANSFORMER_REGEX);
    }

    @Test
    public void testCatalogDevConsole() {
        checkCommandOutputsPattern("catalog dev-console", DEV_CONSOLE_REGEX);
    }

    @Test
    public void testCatalogOther() {
        checkCommandOutputsPattern("catalog other", COMPONENT_REGEX);
    }
}
