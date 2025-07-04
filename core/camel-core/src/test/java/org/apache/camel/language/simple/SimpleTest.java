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
package org.apache.camel.language.simple;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.camel.CamelAuthorizationException;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Expression;
import org.apache.camel.ExpressionIllegalSyntaxException;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.LanguageTestSupport;
import org.apache.camel.Predicate;
import org.apache.camel.component.bean.MethodNotFoundException;
import org.apache.camel.language.bean.RuntimeBeanExpressionException;
import org.apache.camel.language.simple.myconverter.MyCustomDate;
import org.apache.camel.language.simple.types.SimpleIllegalSyntaxException;
import org.apache.camel.spi.ExchangeFormatter;
import org.apache.camel.spi.Language;
import org.apache.camel.spi.PropertiesComponent;
import org.apache.camel.spi.Registry;
import org.apache.camel.spi.UuidGenerator;
import org.apache.camel.spi.VariableRepository;
import org.apache.camel.spi.VariableRepositoryFactory;
import org.apache.camel.support.ExchangeHelper;
import org.apache.camel.support.LanguageHelper;
import org.apache.camel.util.InetAddressUtil;
import org.apache.camel.util.StringHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleTest extends LanguageTestSupport {

    private static final String INDEX_OUT_OF_BOUNDS_ERROR_MSG = "Index 2 out of bounds for length 2";

    @Override
    protected Registry createCamelRegistry() throws Exception {
        Registry jndi = super.createCamelRegistry();
        jndi.bind("myAnimal", new Animal("Donkey", 17));
        return jndi;
    }

    @Test
    public void testSimpleExpressionOrPredicate() {
        Predicate predicate = context.resolveLanguage("simple").createPredicate("${header.bar} == 123");
        assertTrue(predicate.matches(exchange));

        predicate = context.resolveLanguage("simple").createPredicate("${header.bar} == 124");
        assertFalse(predicate.matches(exchange));

        Expression expression = context.resolveLanguage("simple").createExpression("${body}");
        assertEquals("<hello id='m123'>world!</hello>", expression.evaluate(exchange, String.class));

        expression = context.resolveLanguage("simple").createExpression("${body}");
        assertEquals("<hello id='m123'>world!</hello>", expression.evaluate(exchange, String.class));
        expression = context.resolveLanguage("simple").createExpression("${body}");
        assertEquals("<hello id='m123'>world!</hello>", expression.evaluate(exchange, String.class));

        predicate = context.resolveLanguage("simple").createPredicate("${header.bar} == 123");
        assertEquals(Boolean.TRUE, predicate.matches(exchange));
        predicate = context.resolveLanguage("simple").createPredicate("${header.bar} == 124");
        assertEquals(Boolean.FALSE, predicate.matches(exchange));
        predicate = context.resolveLanguage("simple").createPredicate("${header.bar} == 123");
        assertEquals(Boolean.TRUE, predicate.matches(exchange));
        predicate = context.resolveLanguage("simple").createPredicate("${header.bar} == 124");
        assertEquals(Boolean.FALSE, predicate.matches(exchange));
    }

    @Test
    public void testResultType() {
        assertEquals(123, context.resolveLanguage("simple").createExpression("${header.bar}").evaluate(exchange, int.class));
        assertEquals("123",
                context.resolveLanguage("simple").createExpression("${header.bar}").evaluate(exchange, String.class));
        // should not be possible
        assertNull(context.resolveLanguage("simple").createExpression("${header.bar}").evaluate(exchange, Date.class));
        assertNull(context.resolveLanguage("simple").createExpression("${header.unknown}").evaluate(exchange, String.class));
    }

    @Test
    public void testRefExpression() {
        assertExpressionResultInstanceOf("${ref:myAnimal}", Animal.class);

        assertExpression("${ref:myAnimal}", "Donkey");
        assertExpression("${ref:unknown}", null);
        assertExpression("Hello ${ref:myAnimal}", "Hello Donkey");
        assertExpression("Hello ${ref:unknown}", "Hello ");
    }

    @Test
    public void testConstantExpression() {
        assertExpression("Hello World", "Hello World");
    }

    @Test
    public void testNull() {
        assertNull(context.resolveLanguage("simple").createExpression("${null}").evaluate(exchange, Object.class));
    }

    @Test
    public void testSimpleFileDir() {
        assertExpression("file:mydir", "file:mydir");
    }

    @Test
    public void testEmptyExpression() {
        assertExpression("", "");
        assertExpression(" ", " ");

        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class,
                () -> assertExpression(null, null),
                "Should have thrown exception");

        assertEquals("expression must be specified", e1.getMessage());

        assertPredicate("", false);
        assertPredicate(" ", false);

        IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class,
                () -> assertPredicate(null, false),
                "Should have thrown exception");

        assertEquals("expression must be specified", e2.getMessage());
    }

    @Test
    public void testExchangeExpression() {
        Expression exp = context.resolveLanguage("simple").createExpression("${exchange}");
        assertNotNull(exp);

        assertEquals(exchange, exp.evaluate(exchange, Object.class));
        assertExpression("${exchange}", exchange);
    }

    @Test
    public void testLogExchangeExpression() {
        Expression exp = context.resolveLanguage("simple").createExpression("${logExchange}");
        assertNotNull(exp);

        // will use exchange formatter
        ExchangeFormatter ef = LanguageHelper.getOrCreateExchangeFormatter(context, null);
        String expected = ef.format(exchange);
        assertEquals(expected, exp.evaluate(exchange, Object.class));

        assertExpression("${logExchange}", expected);
    }

    @Test
    public void testExchangeOgnlExpression() {
        Expression exp = context.resolveLanguage("simple").createExpression("${exchange.exchangeId}");
        assertNotNull(exp);
        assertEquals(exchange.getExchangeId(), exp.evaluate(exchange, Object.class));

        assertExpression("${exchange.exchangeId}", exchange.getExchangeId());
        assertExpression("${exchange.class.name}", "org.apache.camel.support.DefaultExchange");
    }

    @Test
    public void testBodyExpression() {
        Expression exp = context.resolveLanguage("simple").createExpression("${body}");
        assertNotNull(exp);
    }

    @Test
    public void testBodyOgnlExpression() {
        Expression exp = context.resolveLanguage("simple").createExpression("${body.xxx}");
        assertNotNull(exp);

        // must start with a dot
        assertThrows(SimpleIllegalSyntaxException.class,
                () -> context.resolveLanguage("simple").createExpression("${bodyxxx}"),
                "Should throw exception");
    }

    @Test
    public void testBodyExpressionUsingAlternativeStartToken() {
        Expression exp = context.resolveLanguage("simple").createExpression("$simple{body}");
        assertNotNull(exp);
    }

    @Test
    public void testBodyExpressionNotStringType() {
        exchange.getIn().setBody(123);
        Expression exp = context.resolveLanguage("simple").createExpression("${body}");
        assertNotNull(exp);
        Object val = exp.evaluate(exchange, Object.class);
        assertIsInstanceOf(Integer.class, val);
        assertEquals(123, val);
    }

    @Test
    public void testBodyExpressionWithArray() {
        exchange.getIn().setBody(new MyClass());
        Expression exp = context.resolveLanguage("simple").createExpression("${body.myArray}");
        assertNotNull(exp);
        Object val = exp.evaluate(exchange, Object.class);
        assertIsInstanceOf(Object[].class, val);

        exp = context.resolveLanguage("simple").createExpression("${body.myArray.length}");
        assertNotNull(exp);
        val = exp.evaluate(exchange, Object.class);
        assertIsInstanceOf(Integer.class, val);
        assertEquals(3, val);
    }

    @Test
    public void testSimpleExpressions() {
        assertExpression("${exchangeId}", exchange.getExchangeId());
        assertExpression("${id}", exchange.getIn().getMessageId());
        assertExpression("${body}", "<hello id='m123'>world!</hello>");
        assertExpression("${in.body}", "<hello id='m123'>world!</hello>");
        assertExpression("${in.header.foo}", "abc");
        assertExpression("${in.headers.foo}", "abc");
        assertExpression("${header.foo}", "abc");
        assertExpression("${headers.foo}", "abc");
        assertExpression("${routeId}", ExchangeHelper.getRouteId(exchange));
        assertExpression("${fromRouteId}", exchange.getFromRouteId());
        exchange.getExchangeExtension().setFromRouteId("myRouteId");
        assertExpression("${routeId}", "myRouteId");
    }

    @Test
    public void testTrimSimpleExpressions() {
        assertExpression(" \t${exchangeId}\n".trim(), exchange.getExchangeId());
        assertExpression("\n${id}\r".trim(), exchange.getIn().getMessageId());
        assertExpression("\t\r ${body}".trim(), "<hello id='m123'>world!</hello>");
        assertExpression("\n${in.body}\r".trim(), "<hello id='m123'>world!</hello>");
    }

    @Test
    public void testSimpleThreadId() {
        long id = Thread.currentThread().getId();
        assertExpression("${threadId}", id);
        assertExpression("The id is ${threadId}", "The id is " + id);
    }

    @Test
    public void testSimpleThreadName() {
        String name = Thread.currentThread().getName();
        assertExpression("${threadName}", name);
        assertExpression("The name is ${threadName}", "The name is " + name);
    }

    @Test
    public void testSimpleHostname() {
        String name = InetAddressUtil.getLocalHostNameSafe();
        assertExpression("${hostname}", name);
        assertExpression("The host is ${hostname}", "The host is " + name);
    }

    @Test
    public void testSimpleStepId() {
        assertExpression("${stepId}", null);
        exchange.setProperty(Exchange.STEP_ID, "foo");
        assertExpression("${stepId}", "foo");
    }

    @Test
    public void testSimpleExchangePropertyExpressions() {
        exchange.setProperty("medal", "gold");
        assertExpression("${exchangeProperty.medal}", "gold");
        assertExpression("${exchangeProperty:medal}", "gold");
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    public void testSimpleSystemPropertyExpressions() {
        System.setProperty("who", "I was here");
        assertExpression("${sys.who}", "I was here");
    }

    @Test
    public void testSimpleSystemEnvironmentExpressions() {
        String path = System.getenv("PATH");
        if (path != null) {
            assertExpression("${sysenv.PATH}", path);
            assertExpression("${sysenv:PATH}", path);
            assertExpression("${env.PATH}", path);
            assertExpression("${env:PATH}", path);
        }
    }

    @Test
    public void testSimpleSystemEnvironmentExpressionsIfDash() {
        String foo = System.getenv("FOO_SERVICE_HOST");
        if (foo != null) {
            assertExpression("${sysenv.FOO-SERVICE-HOST}", foo);
            assertExpression("${sysenv:FOO-SERVICE-HOST}", foo);
            assertExpression("${env.FOO-SERVICE-HOST}", foo);
            assertExpression("${env:FOO-SERVICE-HOST}", foo);
        }
    }

    @Test
    public void testSimpleSystemEnvironmentExpressionsIfLowercase() {
        String path = System.getenv("PATH");
        if (path != null) {
            assertExpression("${sysenv.path}", path);
            assertExpression("${sysenv:path}", path);
            assertExpression("${env.path}", path);
            assertExpression("${env:path}", path);
        }
    }

    @Test
    public void testSimpleCamelId() {
        assertExpression("${camelId}", context.getName());
    }

    @Test
    public void testOGNLBodyListAndMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("cool", "Camel rocks");
        map.put("dude", "Hey dude");
        map.put("code", 4321);

        List<Map<String, Object>> lines = new ArrayList<>();
        lines.add(map);

        exchange.getIn().setBody(lines);

        assertExpression("${in.body[0][cool]}", "Camel rocks");
        assertExpression("${body[0][cool]}", "Camel rocks");
        assertExpression("${in.body[0][code]}", 4321);
        assertExpression("${body[0][code]}", 4321);
    }

    @Test
    public void testOGNLBodyEmptyList() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("list", new ArrayList<>());

        exchange.getIn().setBody(map);

        assertExpression("${in.body?.get('list')[0].toString}", null);
    }

    @Test
    public void testOGNLBodyExpression() {
        exchange.getIn().setBody("hello world");
        assertPredicate("${body} == 'hello world'", true);
        assertPredicate("${body.toUpperCase()} == 'HELLO WORLD'", true);
    }

    @Test
    public void testOGNLBodyAsExpression() {
        byte[] body = "hello world".getBytes();
        exchange.getIn().setBody(body);

        // there is no upper case method on byte array, but we can convert to
        // String as below
        RuntimeBeanExpressionException e = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertPredicate("${body.toUpperCase()} == 'HELLO WORLD'", true),
                "Should throw exception");

        MethodNotFoundException cause = assertIsInstanceOf(MethodNotFoundException.class, e.getCause());
        assertEquals("toUpperCase()", cause.getMethodName());

        assertPredicate("${bodyAs(String)} == 'hello world'", true);
        assertPredicate("${bodyAs(String).toUpperCase()} == 'HELLO WORLD'", true);

        // and body on exchange should not be changed
        assertSame(body, exchange.getIn().getBody());
    }

    @Test
    public void testOGNLMandatoryBodyAsExpression() {
        byte[] body = "hello world".getBytes();
        exchange.getIn().setBody(body);

        // there is no upper case method on byte array, but we can convert to
        // String as below
        RuntimeBeanExpressionException e = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertPredicate("${body.toUpperCase()} == 'HELLO WORLD'", true),
                "Should throw exception");

        MethodNotFoundException cause = assertIsInstanceOf(MethodNotFoundException.class, e.getCause());
        assertEquals("toUpperCase()", cause.getMethodName());

        assertPredicate("${mandatoryBodyAs(String)} == 'hello world'", true);
        assertPredicate("${mandatoryBodyAs(String).toUpperCase()} == 'HELLO WORLD'", true);

        // and body on exchange should not be changed
        assertSame(body, exchange.getIn().getBody());
    }

    @Test
    public void testOGNLCallReplace() {
        Map<String, Object> map = new HashMap<>();
        map.put("cool", "Camel rocks");
        map.put("dude", "Hey dude");
        exchange.getIn().setHeaders(map);

        assertExpression("${headers.cool.replaceAll(\"rocks\", \"is so cool\")}", "Camel is so cool");
    }

    @Test
    public void testOGNLBodyListAndMapAndMethod() {
        Map<String, Object> map = new HashMap<>();
        map.put("camel", new OrderLine(123, "Camel in Action"));
        map.put("amq", new OrderLine(456, "ActiveMQ in Action"));

        List<Map<String, Object>> lines = new ArrayList<>();
        lines.add(map);

        exchange.getIn().setBody(lines);

        assertExpression("${in.body[0][camel].id}", 123);
        assertExpression("${in.body[0][camel].name}", "Camel in Action");
        assertExpression("${in.body[0][camel].getId}", 123);
        assertExpression("${in.body[0][camel].getName}", "Camel in Action");
        assertExpression("${body[0][camel].id}", 123);
        assertExpression("${body[0][camel].name}", "Camel in Action");
        assertExpression("${body[0][camel].getId}", 123);
        assertExpression("${body[0][camel].getName}", "Camel in Action");
    }

    @Test
    public void testOGNLPropertyList() {
        List<String> lines = new ArrayList<>();
        lines.add("Camel in Action");
        lines.add("ActiveMQ in Action");
        exchange.setProperty("wicket", lines);

        assertExpression("${exchangeProperty.wicket[0]}", "Camel in Action");
        assertExpression("${exchangeProperty.wicket[1]}", "ActiveMQ in Action");

        Exception e = assertThrows(Exception.class,
                () -> assertExpression("${exchangeProperty.wicket[2]}", ""),
                "Should have thrown an exception");

        IndexOutOfBoundsException cause = assertIsInstanceOf(IndexOutOfBoundsException.class, e.getCause());
        assertEquals(INDEX_OUT_OF_BOUNDS_ERROR_MSG, cause.getMessage());

        assertExpression("${exchangeProperty.unknown[cool]}", null);
    }

    @Test
    public void testOGNLPropertyLinesList() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));
        exchange.setProperty("wicket", lines);

        assertExpression("${exchangeProperty.wicket[0].getId}", 123);
        assertExpression("${exchangeProperty.wicket[1].getName}", "ActiveMQ in Action");

        Exception e = assertThrows(Exception.class,
                () -> assertExpression("${exchangeProperty.wicket[2]}", ""),
                "Should have thrown an exception");

        IndexOutOfBoundsException cause = assertIsInstanceOf(IndexOutOfBoundsException.class, e.getCause());
        assertEquals(INDEX_OUT_OF_BOUNDS_ERROR_MSG, cause.getMessage());

        assertExpression("${exchangeProperty.unknown[cool]}", null);
    }

    @Test
    public void testOGNLPropertyMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("cool", "Camel rocks");
        map.put("dude", "Hey dude");
        map.put("code", 4321);
        exchange.setProperty("wicket", map);

        assertExpression("${exchangeProperty.wicket[cool]}", "Camel rocks");
        assertExpression("${exchangeProperty.wicket[dude]}", "Hey dude");
        assertExpression("${exchangeProperty.wicket[unknown]}", null);
        assertExpression("${exchangeProperty.wicket[code]}", 4321);
        // no header named unknown
        assertExpression("${exchangeProperty?.unknown[cool]}", null);
        assertExpression("${exchangeProperty.unknown[cool]}", null);
    }

    @Test
    public void testOGNLExchangePropertyMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("cool", "Camel rocks");
        map.put("dude", "Hey dude");
        map.put("code", 4321);
        exchange.setProperty("wicket", map);

        assertExpression("${exchangeProperty.wicket[cool]}", "Camel rocks");
        assertExpression("${exchangeProperty.wicket[dude]}", "Hey dude");
        assertExpression("${exchangeProperty.wicket[unknown]}", null);
        assertExpression("${exchangeProperty.wicket[code]}", 4321);
        // no header named unknown
        assertExpression("${exchangeProperty?.unknown[cool]}", null);
        assertExpression("${exchangeProperty.unknown[cool]}", null);
    }

    @Test
    public void testOGNLPropertyMapWithDot() {
        Map<String, Object> map = new HashMap<>();
        map.put("this.code", "This code");
        exchange.setProperty("wicket", map);

        assertExpression("${exchangeProperty.wicket[this.code]}", "This code");
    }

    @Test
    public void testOGNLPropertyMapNotMap() {
        RuntimeBeanExpressionException e = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${exchangeProperty.foobar[bar]}", null),
                "Should have thrown an exception");

        IndexOutOfBoundsException cause = assertIsInstanceOf(IndexOutOfBoundsException.class, e.getCause());
        assertEquals("Key: bar not found in bean: cba of type: java.lang.String using OGNL path [[bar]]",
                cause.getMessage());
    }

    @Test
    public void testOGNLPropertyMapIllegalSyntax() {
        ExpressionIllegalSyntaxException e = assertThrows(ExpressionIllegalSyntaxException.class,
                () -> assertExpression("${exchangeProperty.foobar[bar}", null),
                "Should have thrown an exception");

        assertTrue(e.getMessage()
                .startsWith("Valid syntax: ${exchangeProperty.OGNL} was: exchangeProperty.foobar[bar at location 0"));
    }

    @Test
    public void testOGNLExchangePropertyMapIllegalSyntax() {
        ExpressionIllegalSyntaxException e = assertThrows(ExpressionIllegalSyntaxException.class,
                () -> assertExpression("${exchangeProperty.foobar[bar}", null),
                "Should have thrown an exception");

        assertTrue(e.getMessage()
                .startsWith("Valid syntax: ${exchangeProperty.OGNL} was: exchangeProperty.foobar[bar at location 0"));
    }

    @Test
    public void testOGNLHeaderEmptyTest() {
        exchange.getIn().setHeader("beer", "");
        assertPredicate("${header.beer} == ''", true);
        assertPredicate("${header.beer} == \"\"", true);
        assertPredicate("${header.beer} == ' '", false);
        assertPredicate("${header.beer} == \" \"", false);

        exchange.getIn().setHeader("beer", " ");
        assertPredicate("${header.beer} == ''", false);
        assertPredicate("${header.beer} == \"\"", false);
        assertPredicate("${header.beer} == ' '", true);
        assertPredicate("${header.beer} == \" \"", true);

        assertPredicate("${header.beer.toString().trim()} == ''", true);
        assertPredicate("${header.beer.toString().trim()} == \"\"", true);

        exchange.getIn().setHeader("beer", "   ");
        assertPredicate("${header.beer.trim()} == ''", true);
        assertPredicate("${header.beer.trim()} == \"\"", true);
    }

    @Test
    public void testDateExpressions() {
        Calendar inHeaderCalendar = Calendar.getInstance();
        inHeaderCalendar.set(1974, Calendar.APRIL, 20);
        exchange.getIn().setHeader("birthday", inHeaderCalendar.getTime());

        Calendar propertyCalendar = Calendar.getInstance();
        propertyCalendar.set(1976, Calendar.JUNE, 22);
        exchange.setProperty("birthday", propertyCalendar.getTime());

        assertExpression("${date:header.birthday}", inHeaderCalendar.getTime());
        assertExpression("${date:header.birthday:yyyyMMdd}", "19740420");
        assertExpression("${date:header.birthday+24h:yyyyMMdd}", "19740421");

        // long
        assertExpression("${date:exchangeProperty.birthday}", propertyCalendar.getTime().getTime());
        // date
        assertExpression("${date:exchangeProperty.birthday}", propertyCalendar.getTime());
        assertExpression("${date:exchangeProperty.birthday:yyyyMMdd}", "19760622");
        assertExpression("${date:exchangeProperty.birthday+24h:yyyyMMdd}", "19760623");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> assertExpression("${date:yyyyMMdd}", "19740420"),
                "Should thrown an exception");

        assertEquals("Command not supported for dateExpression: yyyyMMdd", e.getMessage());
    }

    @Test
    public void testDateAndTimeExpressions() {
        Calendar cal = Calendar.getInstance();
        cal.set(1974, Calendar.APRIL, 20, 8, 55, 47);
        cal.set(Calendar.MILLISECOND, 123);
        exchange.getIn().setHeader("birthday", cal.getTime());

        assertExpression("${date:header.birthday - 10s:yyyy-MM-dd'T'HH:mm:ss:SSS}", "1974-04-20T08:55:37:123");
        assertExpression("${date:header.birthday:yyyy-MM-dd'T'HH:mm:ss:SSS}", "1974-04-20T08:55:47:123");
    }

    @Test
    public void testDateWithConverterExpressions() {
        exchange.getIn().setHeader("birthday", new MyCustomDate(1974, Calendar.APRIL, 20));
        exchange.setProperty("birthday", new MyCustomDate(1974, Calendar.APRIL, 20));
        exchange.getIn().setHeader("other", new ArrayList<>());

        assertExpression("${date:header.birthday:yyyyMMdd}", "19740420");
        assertExpression("${date:exchangeProperty.birthday:yyyyMMdd}", "19740420");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> assertExpression("${date:header.other:yyyyMMdd}", "19740420"),
                "Should thrown an exception");

        assertEquals("Cannot find Date/long object at command: header.other", e.getMessage());
    }

    @Test
    public void testDateWithTimezone() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        cal.set(1974, Calendar.APRIL, 20, 8, 55, 47);
        cal.set(Calendar.MILLISECOND, 123);
        exchange.getIn().setHeader("birthday", cal.getTime());

        assertExpression("${date-with-timezone:header.birthday:GMT+8:yyyy-MM-dd'T'HH:mm:ss:SSS}", "1974-04-20T08:55:47:123");
        assertExpression("${date-with-timezone:header.birthday:GMT:yyyy-MM-dd'T'HH:mm:ss:SSS}", "1974-04-20T00:55:47:123");
    }

    @Test
    public void testDateNow() {
        Object out = evaluateExpression("${date:now:hh:mm:ss a}", null);
        assertNotNull(out);
    }

    @Test
    public void testDateExchangeCreated() {
        Object out
                = evaluateExpression("${date:exchangeCreated:hh:mm:ss a}", ("" + exchange.getClock().getCreated()).getClass());
        assertNotNull(out);
    }

    @Test
    public void testDatePredicates() {
        assertPredicate("${date:now} < ${date:now+60s}");
        assertPredicate("${date:now-5s} < ${date:now}");
        assertPredicate("${date:now+5s} > ${date:now}");
    }

    @Test
    public void testLanguagesInContext() {
        // evaluate so we know there is 1 language in the context
        assertExpression("${id}", exchange.getIn().getMessageId());

        assertEquals(1, context.getLanguageNames().size());
        assertEquals("simple", context.getLanguageNames().iterator().next());
    }

    @Test
    public void testComplexExpressions() {
        assertExpression("hey ${in.header.foo}", "hey abc");
        assertExpression("hey ${in.header:foo}", "hey abc");
        assertExpression("hey ${in.header.foo}!", "hey abc!");
        assertExpression("hey ${in.header:foo}!", "hey abc!");
        assertExpression("hey ${in.header.foo}-${in.header.foo}!", "hey abc-abc!");
        assertExpression("hey ${in.header:foo}-${in.header.foo}!", "hey abc-abc!");
        assertExpression("hey ${in.header.foo}${in.header.foo}", "hey abcabc");
        assertExpression("hey ${in.header:foo}${in.header.foo}", "hey abcabc");
        assertExpression("${in.header.foo}${in.header.foo}", "abcabc");
        assertExpression("${in.header:foo}${in.header:foo}", "abcabc");
        assertExpression("${in.header.foo}", "abc");
        assertExpression("${in.header:foo}", "abc");
        assertExpression("${in.header.foo}!", "abc!");
        assertExpression("${in.header:foo}!", "abc!");
    }

    @Test
    public void testComplexExpressionsUsingAlternativeStartToken() {
        assertExpression("hey $simple{in.header.foo}", "hey abc");
        assertExpression("hey $simple{in.header:foo}", "hey abc");
        assertExpression("hey $simple{in.header.foo}!", "hey abc!");
        assertExpression("hey $simple{in.header:foo}!", "hey abc!");
        assertExpression("hey $simple{in.header.foo}-$simple{in.header.foo}!", "hey abc-abc!");
        assertExpression("hey $simple{in.header:foo}-$simple{in.header.foo}!", "hey abc-abc!");
        assertExpression("hey $simple{in.header.foo}$simple{in.header.foo}", "hey abcabc");
        assertExpression("hey $simple{in.header:foo}$simple{in.header.foo}", "hey abcabc");
        assertExpression("$simple{in.header.foo}$simple{in.header.foo}", "abcabc");
        assertExpression("$simple{in.header:foo}$simple{in.header.foo}", "abcabc");
        assertExpression("$simple{in.header.foo}", "abc");
        assertExpression("$simple{in.header:foo}", "abc");
        assertExpression("$simple{in.header.foo}!", "abc!");
        assertExpression("$simple{in.header:foo}!", "abc!");
    }

    @Test
    public void testInvalidComplexExpression() {
        SimpleIllegalSyntaxException e = assertThrows(SimpleIllegalSyntaxException.class,
                () -> assertExpression("hey ${foo", "bad expression!"),
                "Should have thrown an exception!");

        assertEquals(8, e.getIndex());
    }

    @Test
    public void testPredicates() {
        assertPredicate("${body}");
        assertPredicate("${header.foo}");
        assertPredicate("${header.madeUpHeader}", false);
    }

    @Test
    public void testExceptionMessage() {
        exchange.setException(new IllegalArgumentException("Just testing"));
        assertExpression("${exception.message}", "Just testing");
        assertExpression("Hello ${exception.message} World", "Hello Just testing World");
    }

    @Test
    public void testExceptionStacktrace() {
        exchange.setException(new IllegalArgumentException("Just testing"));

        String out = context.resolveLanguage("simple").createExpression("${exception.stacktrace}").evaluate(exchange,
                String.class);
        assertNotNull(out);
        assertTrue(out.startsWith("java.lang.IllegalArgumentException: Just testing"));
        assertTrue(out.contains("at org.apache.camel.language."));
    }

    @Test
    public void testException() {
        exchange.setException(new IllegalArgumentException("Just testing"));

        Exception out = context.resolveLanguage("simple").createExpression("${exception}").evaluate(exchange, Exception.class);
        assertNotNull(out);
        assertIsInstanceOf(IllegalArgumentException.class, out);
        assertEquals("Just testing", out.getMessage());
    }

    @Test
    public void testMessageAs() {
        // should be false as message is default
        assertPredicate("${messageAs(org.apache.camel.language.simple.MyAttachmentMessage).hasAttachments}", false);
        assertPredicate("${messageAs(org.apache.camel.language.simple.MyAttachmentMessage)?.hasAttachments}", false);

        MyAttachmentMessage msg = new MyAttachmentMessage(exchange);
        msg.setBody("<hello id='m123'>world!</hello>");
        exchange.setMessage(msg);

        assertPredicate("${messageAs(org.apache.camel.language.simple.MyAttachmentMessage).hasAttachments}", true);
        assertPredicate("${messageAs(org.apache.camel.language.simple.MyAttachmentMessage)?.hasAttachments}", true);
        assertExpression("${messageAs(org.apache.camel.language.simple.MyAttachmentMessage).size}", "42");
    }

    @Test
    public void testBodyAs() {
        assertExpression("${bodyAs(String)}", "<hello id='m123'>world!</hello>");
        assertExpression("${bodyAs('String')}", "<hello id='m123'>world!</hello>");

        exchange.getIn().setBody(null);
        assertExpression("${bodyAs('String')}", null);

        exchange.getIn().setBody(456);
        assertExpression("${bodyAs(Integer)}", 456);
        assertExpression("${bodyAs(int)}", 456);
        assertExpression("${bodyAs('int')}", 456);

        CamelExecutionException e = assertThrows(CamelExecutionException.class,
                () -> assertExpression("${bodyAs(XXX)}", 456),
                "Should have thrown an exception");

        assertIsInstanceOf(ClassNotFoundException.class, e.getCause());
    }

    @Test
    public void testMandatoryBodyAs() {
        assertExpression("${mandatoryBodyAs(String)}", "<hello id='m123'>world!</hello>");
        assertExpression("${mandatoryBodyAs('String')}", "<hello id='m123'>world!</hello>");

        exchange.getIn().setBody(null);
        CamelExecutionException e1 = assertThrows(CamelExecutionException.class,
                () -> assertExpression("${mandatoryBodyAs('String')}", ""),
                "Should have thrown exception");

        assertIsInstanceOf(InvalidPayloadException.class, e1.getCause());

        exchange.getIn().setBody(456);
        assertExpression("${mandatoryBodyAs(Integer)}", 456);
        assertExpression("${mandatoryBodyAs(int)}", 456);
        assertExpression("${mandatoryBodyAs('int')}", 456);

        CamelExecutionException e2 = assertThrows(CamelExecutionException.class,
                () -> assertExpression("${mandatoryBodyAs(XXX)}", 456),
                "Should have thrown an exception");

        assertIsInstanceOf(ClassNotFoundException.class, e2.getCause());
    }

    @Test
    public void testHeaderEmptyBody() {
        // set an empty body
        exchange.getIn().setBody(null);

        assertExpression("${header.foo}", "abc");
        assertExpression("${header:foo}", "abc");
        assertExpression("${headers.foo}", "abc");
        assertExpression("${headers:foo}", "abc");
        assertExpression("${in.header.foo}", "abc");
        assertExpression("${in.header:foo}", "abc");
        assertExpression("${in.headers.foo}", "abc");
        assertExpression("${in.headers:foo}", "abc");
    }

    @Test
    public void testHeadersWithBracket() {
        assertExpression("${headers[foo]}", "abc");
        assertExpression("${in.headers[foo]}", "abc");
    }

    @Test
    public void testOnglOnHeadersWithBracket() {
        assertOnglOnHeadersWithSquareBrackets("order");
        assertOnglOnHeadersWithSquareBrackets("purchase.order");
        assertOnglOnHeadersWithSquareBrackets("foo.bar.qux");
        assertOnglOnHeadersWithSquareBrackets("purchase order");
    }

    private void assertOnglOnHeadersWithSquareBrackets(String key) {
        exchange.getIn().setHeader(key, new OrderLine(123, "Camel in Action"));
        assertExpression("${headers[" + key + "].name}", "Camel in Action");
        assertExpression("${in.headers[" + key + "].name}", "Camel in Action");
        assertExpression("${in.headers['" + key + "'].name}", "Camel in Action");
    }

    @Test
    public void testOnglOnExchangePropertiesWithBracket() throws Exception {
        assertOnglOnExchangePropertiesWithBracket("order");
        assertOnglOnExchangePropertiesWithBracket("purchase.order");
        assertOnglOnExchangePropertiesWithBracket("foo.bar.qux");
        assertOnglOnExchangePropertiesWithBracket("purchase order");
    }

    public void assertOnglOnExchangePropertiesWithBracket(String key) {
        exchange.setProperty(key, new OrderLine(123, "Camel in Action"));
        assertExpression("${exchangeProperty[" + key + "].name}", "Camel in Action");
        assertExpression("${exchangeProperty['" + key + "'].name}", "Camel in Action");
    }

    @Test
    public void testIsInstanceOfEmptyBody() {
        // set an empty body
        exchange.getIn().setBody(null);

        SimpleIllegalSyntaxException e = assertThrows(SimpleIllegalSyntaxException.class,
                () -> assertPredicate("${body} is null", false),
                "Should have thrown an exception");

        assertEquals(11, e.getIndex());
    }

    @Test
    public void testHeaders() {
        Map<String, Object> headers = exchange.getIn().getHeaders();
        assertEquals(2, headers.size());

        assertExpression("${headers}", headers);
        assertExpression("${in.headers}", headers);
        assertExpression("${headers.size}", 2);
    }

    @Test
    public void testHeaderKeyWithSpace() {
        Map<String, Object> headers = exchange.getIn().getHeaders();
        headers.put("some key", "Some Value");
        assertEquals(3, headers.size());

        assertExpression("${headerAs(foo,String)}", "abc");
        assertExpression("${headerAs(some key,String)}", "Some Value");
        assertExpression("${headerAs('some key',String)}", "Some Value");

        assertExpression("${header[foo]}", "abc");
        assertExpression("${header[some key]}", "Some Value");
        assertExpression("${header['some key']}", "Some Value");

        assertExpression("${headers[foo]}", "abc");
        assertExpression("${headers[some key]}", "Some Value");
        assertExpression("${headers['some key']}", "Some Value");
    }

    @Test
    public void testHeaderAs() {
        assertExpression("${headerAs(foo,String)}", "abc");

        assertExpression("${headerAs(bar,int)}", 123);
        assertExpression("${headerAs(bar, int)}", 123);
        assertExpression("${headerAs('bar', int)}", 123);
        assertExpression("${headerAs('bar','int')}", 123);
        assertExpression("${headerAs('bar','Integer')}", 123);
        assertExpression("${headerAs('bar',\"int\")}", 123);
        assertExpression("${headerAs(bar,String)}", "123");

        assertExpression("${headerAs(unknown,String)}", null);

        ExpressionIllegalSyntaxException e1 = assertThrows(ExpressionIllegalSyntaxException.class,
                () -> assertExpression("${headerAs(unknown String)}", null),
                "Should have thrown an exception");

        assertTrue(e1.getMessage().startsWith("Valid syntax: ${headerAs(key, type)} was: headerAs(unknown String)"));

        ExpressionIllegalSyntaxException e2 = assertThrows(ExpressionIllegalSyntaxException.class,
                () -> assertExpression("${headerAs(fool,String).test}", null),
                "Should have thrown an exception");

        assertTrue(e2.getMessage().startsWith("Valid syntax: ${headerAs(key, type)} was: headerAs(fool,String).test"));

        CamelExecutionException e3 = assertThrows(CamelExecutionException.class,
                () -> assertExpression("${headerAs(bar,XXX)}", 123),
                "Should have thrown an exception");

        assertIsInstanceOf(ClassNotFoundException.class, e3.getCause());
    }

    @Test
    public void testVariables() {
        exchange.getVariables().putAll(exchange.getMessage().getHeaders());
        exchange.getMessage().removeHeaders("*");

        Map<String, Object> variables = exchange.getVariables();
        assertEquals(3, variables.size());

        assertExpression("${variables}", variables);
        assertExpression("${variables.size}", 3);
    }

    @Test
    public void testGlobalVariable() {
        // exchange has 1 variable already set
        Map<String, Object> variables = exchange.getVariables();
        assertEquals(1, variables.size());

        VariableRepository global = context.getCamelContextExtension().getContextPlugin(VariableRepositoryFactory.class)
                .getVariableRepository("global");
        global.setVariable("foo", "123");
        global.setVariable("bar", "456");
        global.setVariable("cheese", "gorgonzola");

        // exchange scoped
        assertExpression("${variable.cheese}", "gauda");
        assertExpression("${variable.foo}", null);
        assertExpression("${variable.bar}", null);

        // global scoped
        assertExpression("${variable.global:cheese}", "gorgonzola");
        assertExpression("${variable.global:foo}", "123");
        assertExpression("${variable.global:bar}", "456");

        // exchange scoped
        assertExpression("${variableAs('cheese', 'String')}", "gauda");
        assertExpression("${variableAs('foo', 'int')}", null);
        assertExpression("${variableAA('bar', 'int')}", null);

        // global scoped
        assertExpression("${variableAs('global:cheese', 'String')}", "gorgonzola");
        assertExpression("${variableAs('global:foo', 'int')}", 123);
        assertExpression("${variableAs('global:bar', 'int')}", 456);
    }

    @Test
    public void testVariableKeyWithSpace() {
        exchange.getVariables().putAll(exchange.getMessage().getHeaders());
        exchange.getMessage().removeHeaders("*");

        Map<String, Object> variables = exchange.getVariables();
        variables.put("some key", "Some Value");
        assertEquals(4, variables.size());

        assertExpression("${variableAs(foo,String)}", "abc");
        assertExpression("${variableAs(some key,String)}", "Some Value");
        assertExpression("${variableAs('some key',String)}", "Some Value");

        assertExpression("${variable[foo]}", "abc");
        assertExpression("${variable[cheese]}", "gauda");
        assertExpression("${variable[some key]}", "Some Value");
        assertExpression("${variable['some key']}", "Some Value");

        assertExpression("${variables[foo]}", "abc");
        assertExpression("${variables[cheese]}", "gauda");
        assertExpression("${variables[some key]}", "Some Value");
        assertExpression("${variables['some key']}", "Some Value");
    }

    @Test
    public void testVariableAs() {
        exchange.getVariables().putAll(exchange.getMessage().getHeaders());
        exchange.getMessage().removeHeaders("*");

        assertExpression("${variableAs(foo,String)}", "abc");

        assertExpression("${variableAs(bar,int)}", 123);
        assertExpression("${variableAs(bar, int)}", 123);
        assertExpression("${variableAs('bar', int)}", 123);
        assertExpression("${variableAs('bar','int')}", 123);
        assertExpression("${variableAs('bar','Integer')}", 123);
        assertExpression("${variableAs('bar',\"int\")}", 123);
        assertExpression("${variableAs(bar,String)}", "123");

        assertExpression("${variableAs(unknown,String)}", null);

        ExpressionIllegalSyntaxException e1 = assertThrows(ExpressionIllegalSyntaxException.class,
                () -> assertExpression("${variableAs(unknown String)}", null),
                "Should have thrown an exception");

        assertTrue(e1.getMessage().startsWith("Valid syntax: ${variableAs(key, type)} was: variableAs(unknown String)"));

        ExpressionIllegalSyntaxException e2 = assertThrows(ExpressionIllegalSyntaxException.class,
                () -> assertExpression("${variableAs(fool,String).test}", null),
                "Should have thrown an exception");

        assertTrue(e2.getMessage().startsWith("Valid syntax: ${variableAs(key, type)} was: variableAs(fool,String).test"));

        CamelExecutionException e3 = assertThrows(CamelExecutionException.class,
                () -> assertExpression("${variableAs(bar,XXX)}", 123),
                "Should have thrown an exception");

        assertIsInstanceOf(ClassNotFoundException.class, e3.getCause());
    }

    @Test
    public void testIllegalSyntax() {
        ExpressionIllegalSyntaxException e1 = assertThrows(ExpressionIllegalSyntaxException.class,
                () -> assertExpression("hey ${xxx} how are you?", ""),
                "Should have thrown an exception");

        assertTrue(e1.getMessage().startsWith("Unknown function: xxx at location 4"));

        ExpressionIllegalSyntaxException e2 = assertThrows(ExpressionIllegalSyntaxException.class,
                () -> assertExpression("${xxx}", ""),
                "Should have thrown an exception");

        assertTrue(e2.getMessage().startsWith("Unknown function: xxx at location 0"));

        ExpressionIllegalSyntaxException e3 = assertThrows(ExpressionIllegalSyntaxException.class,
                () -> assertExpression("${bodyAs(xxx}", ""),
                "Should have thrown an exception");

        assertTrue(e3.getMessage().startsWith("Valid syntax: ${bodyAs(type)} was: bodyAs(xxx"));
    }

    @Test
    public void testOGNLHeaderList() {
        List<String> lines = new ArrayList<>();
        lines.add("Camel in Action");
        lines.add("ActiveMQ in Action");
        exchange.getIn().setHeader("wicket", lines);

        assertExpression("${header.wicket[0]}", "Camel in Action");
        assertExpression("${header.wicket[1]}", "ActiveMQ in Action");
        Exception e = assertThrows(Exception.class,
                () -> assertExpression("${header.wicket[2]}", ""),
                "Should have thrown an exception");

        IndexOutOfBoundsException cause = assertIsInstanceOf(IndexOutOfBoundsException.class, e.getCause());
        assertEquals(INDEX_OUT_OF_BOUNDS_ERROR_MSG, cause.getMessage());

        assertExpression("${header.unknown[cool]}", null);
    }

    @Test
    public void testOGNLHeaderLinesList() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));
        exchange.getIn().setHeader("wicket", lines);

        assertExpression("${header.wicket[0].getId}", 123);
        assertExpression("${header.wicket[1].getName}", "ActiveMQ in Action");
        Exception e = assertThrows(Exception.class,
                () -> assertExpression("${header.wicket[2]}", ""),
                "Should have thrown an exception");

        IndexOutOfBoundsException cause = assertIsInstanceOf(IndexOutOfBoundsException.class, e.getCause());
        assertEquals(INDEX_OUT_OF_BOUNDS_ERROR_MSG, cause.getMessage());

        assertExpression("${header.unknown[cool]}", null);
    }

    @Test
    public void testOGNLHeaderMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("cool", "Camel rocks");
        map.put("dude", "Hey dude");
        map.put("code", 4321);
        exchange.getIn().setHeader("wicket", map);

        assertExpression("${header.wicket[cool]}", "Camel rocks");
        assertExpression("${header.wicket[dude]}", "Hey dude");
        assertExpression("${header.wicket[unknown]}", null);
        assertExpression("${header.wicket[code]}", 4321);
        // no header named unknown
        assertExpression("${header?.unknown[cool]}", null);
        assertExpression("${header.unknown[cool]}", null);
    }

    @Test
    public void testOGNLHeaderMapWithDot() {
        Map<String, Object> map = new HashMap<>();
        map.put("this.code", "This code");
        exchange.getIn().setHeader("wicket", map);

        assertExpression("${header.wicket[this.code]}", "This code");
    }

    @Test
    public void testOGNLHeaderMapNotMap() {
        RuntimeBeanExpressionException e = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${header.foo[bar]}", null),
                "Should have thrown an exception");

        IndexOutOfBoundsException cause = assertIsInstanceOf(IndexOutOfBoundsException.class, e.getCause());
        assertEquals("Key: bar not found in bean: abc of type: java.lang.String using OGNL path [[bar]]",
                cause.getMessage());
    }

    @Test
    public void testOGNLHeaderMapIllegalSyntax() {
        ExpressionIllegalSyntaxException e = assertThrows(ExpressionIllegalSyntaxException.class,
                () -> assertExpression("${header.foo[bar}", null),
                "Should have thrown an exception");

        assertTrue(e.getMessage().startsWith("Valid syntax: ${header.name[key]} was: header.foo[bar"));
    }

    @Test
    public void testBodyOGNLAsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "Camel");
        map.put("bar", 6);
        exchange.getIn().setBody(map);

        assertExpression("${in.body[foo]}", "Camel");
        assertExpression("${in.body[bar]}", 6);
    }

    @Test
    public void testBodyOGNLAsMapWithDot() {
        Map<String, Object> map = new HashMap<>();
        map.put("foo.bar", "Camel");
        exchange.getIn().setBody(map);

        assertExpression("${in.body[foo.bar]}", "Camel");
    }

    @Test
    public void testBodyOGNLAsMapShorthand() {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "Camel");
        map.put("bar", 6);
        exchange.getIn().setBody(map);

        assertExpression("${body[foo]}", "Camel");
        assertExpression("${body[bar]}", 6);
    }

    @Test
    public void testBodyOGNLSimple() {
        Animal camel = new Animal("Camel", 6);
        exchange.getIn().setBody(camel);

        assertExpression("${in.body.getName}", "Camel");
        assertExpression("${in.body.getAge}", 6);
    }

    @Test
    public void testExceptionOGNLSimple() {
        exchange.getIn().setHeader(Exchange.AUTHENTICATION_FAILURE_POLICY_ID, "myPolicy");
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT,
                new CamelAuthorizationException("The camel authorization exception", exchange));

        assertExpression("${exception.getPolicyId}", "myPolicy");
    }

    @Test
    public void testBodyOGNLSimpleShorthand() {
        Animal camel = new Animal("Camel", 6);
        exchange.getIn().setBody(camel);

        assertExpression("${in.body.name}", "Camel");
        assertExpression("${in.body.age}", 6);
    }

    @Test
    public void testBodyOGNLSimpleOperator() {
        Animal tiger = new Animal("Tony the Tiger", 13);
        Animal camel = new Animal("Camel", 6);
        camel.setFriend(tiger);

        exchange.getIn().setBody(camel);

        assertPredicate("${in.body.getName} contains 'Camel'", true);
        assertPredicate("${in.body.getName} contains 'Tiger'", false);
        assertPredicate("${in.body.getAge} < 10", true);
        assertPredicate("${in.body.getAge} > 10", false);
        assertPredicate("${in.body.getAge} <= '6'", true);
        assertPredicate("${in.body.getAge} > '6'", false);

        assertPredicate("${in.body.getAge} < ${body.getFriend.getAge}", true);
        assertPredicate("${in.body.getFriend.isDangerous} == true", true);
    }

    @Test
    public void testBodyOGNLSimpleOperatorShorthand() {
        Animal tiger = new Animal("Tony the Tiger", 13);
        Animal camel = new Animal("Camel", 6);
        camel.setFriend(tiger);

        exchange.getIn().setBody(camel);

        assertPredicate("${in.body.name} contains 'Camel'", true);
        assertPredicate("${in.body.name} contains 'Tiger'", false);
        assertPredicate("${in.body.age} < 10", true);
        assertPredicate("${in.body.age} > 10", false);
        assertPredicate("${in.body.age} <= '6'", true);
        assertPredicate("${in.body.age} > '6'", false);

        assertPredicate("${in.body.age} < ${body.friend.age}", true);
        assertPredicate("${in.body.friend.dangerous} == true", true);
    }

    @Test
    public void testBodyOGNLNested() {
        Animal tiger = new Animal("Tony the Tiger", 13);
        Animal camel = new Animal("Camel", 6);
        camel.setFriend(tiger);

        exchange.getIn().setBody(camel);

        assertExpression("${in.body.getName}", "Camel");
        assertExpression("${in.body.getAge}", 6);

        assertExpression("${in.body.getFriend.getName}", "Tony the Tiger");
        assertExpression("${in.body.getFriend.getAge}", "13");
    }

    @Test
    public void testBodyOGNLNestedShorthand() {
        Animal tiger = new Animal("Tony the Tiger", 13);
        Animal camel = new Animal("Camel", 6);
        camel.setFriend(tiger);

        exchange.getIn().setBody(camel);

        assertExpression("${in.body.name}", "Camel");
        assertExpression("${in.body.age}", 6);

        assertExpression("${in.body.friend.name}", "Tony the Tiger");
        assertExpression("${in.body.friend.age}", "13");
    }

    @Test
    public void testBodyOGNLOrderList() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));
        Order order = new Order(lines);

        exchange.getIn().setBody(order);

        assertExpression("${in.body.getLines[0].getId}", 123);
        assertExpression("${in.body.getLines[0].getName}", "Camel in Action");

        assertExpression("${in.body.getLines[1].getId}", 456);
        assertExpression("${in.body.getLines[1].getName}", "ActiveMQ in Action");

        assertExpression("${in.body.getLines[last].getId}", 456);
        assertExpression("${in.body.getLines[last].getName}", "ActiveMQ in Action");

        assertExpression("${in.body.getLines[last-1].getId}", 123);
        assertExpression("${in.body.getLines[last-1].getName}", "Camel in Action");
    }

    @Test
    public void testBodyOGNLOrderListShorthand() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));
        Order order = new Order(lines);

        exchange.getIn().setBody(order);

        assertExpression("${in.body.lines[0].id}", 123);
        assertExpression("${in.body.lines[0].name}", "Camel in Action");

        assertExpression("${in.body.lines[1].id}", 456);
        assertExpression("${in.body.lines[1].name}", "ActiveMQ in Action");

        assertExpression("${in.body.lines[last].id}", 456);
        assertExpression("${in.body.lines[last].name}", "ActiveMQ in Action");

        assertExpression("${in.body.lines[last-1].id}", 123);
        assertExpression("${in.body.lines[last-1].name}", "Camel in Action");

        assertExpression("${in.body.lines.size}", 2);
    }

    @Test
    public void testBodyOGNLListMap() {
        List<Map<String, String>> grid = new ArrayList<>();
        Map<String, String> cells = new LinkedHashMap<>();
        cells.put("ABC", "123");
        cells.put("DEF", "456");
        grid.add(cells);

        Map<String, String> cells2 = new LinkedHashMap<>();
        cells2.put("HIJ", "789");
        grid.add(cells2);

        exchange.getIn().setBody(grid);

        assertExpression("${in.body[0][ABC]}", "123");
        assertExpression("${in.body[0][DEF]}", "456");
        assertExpression("${in.body[0]['ABC']}", "123");
        assertExpression("${in.body[0]['DEF']}", "456");
        assertExpression("${in.body[1][HIJ]}", "789");
        assertExpression("${in.body[1]['HIJ']}", "789");
    }

    @Test
    public void testBodyOGNLList() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));

        exchange.getIn().setBody(lines);

        assertExpression("${in.body[0].getId}", 123);
        assertExpression("${in.body[0].getName}", "Camel in Action");

        assertExpression("${in.body[1].getId}", 456);
        assertExpression("${in.body[1].getName}", "ActiveMQ in Action");
    }

    @Test
    public void testBodyOGNLListShorthand() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));

        exchange.getIn().setBody(lines);

        assertExpression("${in.body[0].id}", 123);
        assertExpression("${in.body[0].name}", "Camel in Action");

        assertExpression("${in.body[1].id}", 456);
        assertExpression("${in.body[1].name}", "ActiveMQ in Action");
    }

    @Test
    public void testBodyOGNLArray() {
        OrderLine[] lines = new OrderLine[2];
        lines[0] = new OrderLine(123, "Camel in Action");
        lines[1] = new OrderLine(456, "ActiveMQ in Action");

        exchange.getIn().setBody(lines);

        assertExpression("${in.body[0].getId}", 123);
        assertExpression("${in.body[0].getName}", "Camel in Action");

        assertExpression("${in.body[1].getId}", 456);
        assertExpression("${in.body[1].getName}", "ActiveMQ in Action");
    }

    @Test
    public void testBodyOGNLArrayShorthand() {
        OrderLine[] lines = new OrderLine[2];
        lines[0] = new OrderLine(123, "Camel in Action");
        lines[1] = new OrderLine(456, "ActiveMQ in Action");

        exchange.getIn().setBody(lines);

        assertExpression("${in.body[0].id}", 123);
        assertExpression("${in.body[0].name}", "Camel in Action");

        assertExpression("${in.body[1].id}", 456);
        assertExpression("${in.body[1].name}", "ActiveMQ in Action");
    }

    @Test
    public void testBodyOGNLOrderListOutOfBounds() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));
        Order order = new Order(lines);

        exchange.getIn().setBody(order);

        RuntimeBeanExpressionException e1 = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${in.body.getLines[3].getId}", 123),
                "Should have thrown an exception");

        IndexOutOfBoundsException cause1 = assertIsInstanceOf(IndexOutOfBoundsException.class, e1.getCause());
        assertTrue(cause1.getMessage().startsWith("Index: 3, Size: 2 out of bounds with List from bean"));

        RuntimeBeanExpressionException e2 = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${in.body.getLines[last-2].getId}", 123),
                "Should have thrown an exception");

        IndexOutOfBoundsException cause2 = assertIsInstanceOf(IndexOutOfBoundsException.class, e2.getCause());
        assertTrue(cause2.getMessage().startsWith("Index: -1, Size: 2 out of bounds with List from bean"));

        RuntimeBeanExpressionException e3 = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${in.body.getLines[last - XXX].getId}", 123),
                "Should have thrown an exception");

        ExpressionIllegalSyntaxException cause3 = assertIsInstanceOf(ExpressionIllegalSyntaxException.class, e3.getCause());
        assertEquals("last - XXX", cause3.getExpression());
    }

    @Test
    public void testBodyOGNLOrderListOutOfBoundsShorthand() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));
        Order order = new Order(lines);

        exchange.getIn().setBody(order);

        RuntimeBeanExpressionException e1 = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${in.body.lines[3].id}", 123),
                "Should have thrown an exception");

        IndexOutOfBoundsException cause1 = assertIsInstanceOf(IndexOutOfBoundsException.class, e1.getCause());
        assertTrue(cause1.getMessage().startsWith("Index: 3, Size: 2 out of bounds with List from bean"));

        RuntimeBeanExpressionException e2 = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${in.body.lines[last - 2].id}", 123),
                "Should have thrown an exception");

        IndexOutOfBoundsException cause2 = assertIsInstanceOf(IndexOutOfBoundsException.class, e2.getCause());
        assertTrue(cause2.getMessage().startsWith("Index: -1, Size: 2 out of bounds with List from bean"));

        RuntimeBeanExpressionException e3 = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${in.body.lines[last - XXX].id}", 123),
                "Should have thrown an exception");

        ExpressionIllegalSyntaxException cause3 = assertIsInstanceOf(ExpressionIllegalSyntaxException.class, e3.getCause());
        assertEquals("last - XXX", cause3.getExpression());
    }

    @Test
    public void testBodyOGNLOrderListOutOfBoundsWithNullSafe() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));
        Order order = new Order(lines);

        exchange.getIn().setBody(order);

        assertExpression("${in.body?.getLines[3].getId}", null);
    }

    @Test
    public void testBodyOGNLOrderListOutOfBoundsWithNullSafeShorthand() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));
        Order order = new Order(lines);

        exchange.getIn().setBody(order);

        assertExpression("${in.body?.lines[3].id}", null);
    }

    @Test
    public void testBodyOGNLOrderListNoMethodNameWithNullSafe() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));
        Order order = new Order(lines);

        exchange.getIn().setBody(order);

        RuntimeBeanExpressionException e = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${in.body.getLines[0]?.getRating}", ""),
                "Should have thrown exception");

        MethodNotFoundException cause = assertIsInstanceOf(MethodNotFoundException.class, e.getCause());
        assertEquals("getRating", cause.getMethodName());
    }

    @Test
    public void testBodyOGNLOrderListNoMethodNameWithNullSafeShorthand() {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine(123, "Camel in Action"));
        lines.add(new OrderLine(456, "ActiveMQ in Action"));
        Order order = new Order(lines);

        exchange.getIn().setBody(order);

        RuntimeBeanExpressionException e = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${in.body.lines[0]?.rating}", ""),
                "Should have thrown exception");

        MethodNotFoundException cause = assertIsInstanceOf(MethodNotFoundException.class, e.getCause());
        assertEquals("rating", cause.getMethodName());
    }

    @Test
    public void testBodyOGNLNullSafeToAvoidNPE() {
        Animal tiger = new Animal("Tony the Tiger", 13);
        Animal camel = new Animal("Camel", 6);
        camel.setFriend(tiger);

        exchange.getIn().setBody(camel);

        assertExpression("${in.body.getName}", "Camel");
        assertExpression("${in.body.getAge}", 6);

        assertExpression("${in.body.getFriend.getName}", "Tony the Tiger");
        assertExpression("${in.body.getFriend.getAge}", "13");

        // using null safe to avoid the NPE
        assertExpression("${in.body.getFriend?.getFriend.getName}", null);

        // without null safe we get an NPE
        RuntimeBeanExpressionException e = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${in.body.getFriend.getFriend.getName}", ""),
                "Should have thrown exception");

        assertEquals(
                "Failed to invoke method: .getFriend.getFriend.getName on org.apache.camel.language.simple.SimpleTest.Animal"
                     + " due last method returned null and therefore cannot continue to invoke method .getName on a null instance",
                e.getMessage());
    }

    @Test
    public void testBodyOGNLNullSafeToAvoidNPEShorthand() {
        Animal tiger = new Animal("Tony the Tiger", 13);
        Animal camel = new Animal("Camel", 6);
        camel.setFriend(tiger);

        exchange.getIn().setBody(camel);

        assertExpression("${in.body.name}", "Camel");
        assertExpression("${in.body.age}", 6);

        // just to mix it a bit
        assertExpression("${in.body.friend.getName}", "Tony the Tiger");
        assertExpression("${in.body.getFriend.age}", "13");

        // using null safe to avoid the NPE
        assertExpression("${in.body.friend?.friend.name}", null);

        // without null safe we get an NPE
        RuntimeBeanExpressionException e = assertThrows(RuntimeBeanExpressionException.class,
                () -> assertExpression("${in.body.friend.friend.name}", ""),
                "Should have thrown exception");

        assertEquals("Failed to invoke method: .friend.friend.name on org.apache.camel.language.simple.SimpleTest.Animal"
                     + " due last method returned null and therefore cannot continue to invoke method .name on a null instance",
                e.getMessage());
    }

    @Test
    public void testBodyOGNLReentrant() {
        Animal camel = new Animal("Camel", 6);
        Animal tiger = new Animal("Tony the Tiger", 13);
        Animal elephant = new Animal("Big Ella", 48);

        camel.setFriend(tiger);
        tiger.setFriend(elephant);
        elephant.setFriend(camel);

        exchange.getIn().setBody(camel);

        assertExpression("${body.getFriend.getFriend.getFriend.getName}", "Camel");
        assertExpression("${body.getFriend.getFriend.getFriend.getFriend.getName}", "Tony the Tiger");
        assertExpression("${body.getFriend.getFriend.getFriend.getFriend.getFriend.getName}", "Big Ella");
    }

    @Test
    public void testBodyOGNLReentrantShorthand() {
        Animal camel = new Animal("Camel", 6);
        Animal tiger = new Animal("Tony the Tiger", 13);
        Animal elephant = new Animal("Big Ella", 48);

        camel.setFriend(tiger);
        tiger.setFriend(elephant);
        elephant.setFriend(camel);

        exchange.getIn().setBody(camel);

        assertExpression("${body.friend.friend.friend.name}", "Camel");
        assertExpression("${body.friend.friend.friend.friend.name}", "Tony the Tiger");
        assertExpression("${body.friend.friend.friend.friend.friend.name}", "Big Ella");
    }

    @Test
    public void testBodyOGNLBoolean() {
        Animal tiger = new Animal("Tony the Tiger", 13);
        exchange.getIn().setBody(tiger);

        assertExpression("${body.isDangerous}", "true");
        assertExpression("${body.dangerous}", "true");

        Animal camel = new Animal("Camel", 6);
        exchange.getIn().setBody(camel);

        assertExpression("${body.isDangerous}", "false");
        assertExpression("${body.dangerous}", "false");
    }

    @Test
    public void testBodyOgnlOnString() {
        exchange.getIn().setBody("Camel");

        assertExpression("${body.substring(2)}", "mel");
        assertExpression("${body.substring(2, 4)}", "me");
        assertExpression("${body.length()}", 5);
        assertExpression("${body.toUpperCase()}", "CAMEL");
        assertExpression("${body.toUpperCase()}", "CAMEL");
        assertExpression("${body.toUpperCase().substring(2)}", "MEL");
        assertExpression("${body.toLowerCase().length()}", 5);
    }

    @Test
    public void testBodyOgnlOnStringWithOgnlParams() {
        exchange.getIn().setBody("Camel");
        exchange.getIn().setHeader("max", 4);
        exchange.getIn().setHeader("min", 2);

        assertExpression("${body.substring(${header.min}, ${header.max})}", "me");
    }

    @Test
    public void testHeaderOgnlOnStringWithOgnlParams() {
        exchange.getIn().setBody(null);
        exchange.getIn().setHeader("name", "Camel");
        exchange.getIn().setHeader("max", 4);
        exchange.getIn().setHeader("min", 2);

        assertExpression("${header.name.substring(${header.min}, ${header.max})}", "me");
    }

    @Test
    public void testCamelContextStartRoute() {
        exchange.getIn().setBody(null);

        assertExpression("${camelContext.getRouteController().startRoute('foo')}", null);
    }

    @Test
    public void testBodyOgnlReplace() {
        exchange.getIn().setBody("Kamel is a cool Kamel");

        assertExpression("${body.replace(\"Kamel\", \"Camel\")}", "Camel is a cool Camel");
    }

    @Test
    public void testBodyOgnlReplaceEscapedChar() {
        exchange.getIn().setBody("foo$bar$baz");

        assertExpression("${body.replace('$', '-')}", "foo-bar-baz");
    }

    @Test
    public void testBodyOgnlReplaceEscapedBackslashChar() {
        exchange.getIn().setBody("foo\\bar\\baz");

        assertExpression("${body.replace('\\', '\\\\')}", "foo\\\\bar\\\\baz");
    }

    @Test
    public void testBodyOgnlReplaceFirst() {
        exchange.getIn().setBody("http:camel.apache.org");

        assertExpression("${body.replaceFirst('http:', 'https:')}", "https:camel.apache.org");
        assertExpression("${body.replaceFirst('http:', '')}", "camel.apache.org");
        assertExpression("${body.replaceFirst('http:', ' ')}", " camel.apache.org");
        assertExpression("${body.replaceFirst('http:',    ' ')}", " camel.apache.org");
        assertExpression("${body.replaceFirst('http:',' ')}", " camel.apache.org");
    }

    @Test
    public void testBodyOgnlReplaceSingleQuoteInDouble() {
        exchange.getIn().setBody("Hello O'Conner");

        assertExpression("${body.replace(\"O'C\", \"OC\")}", "Hello OConner");
        assertExpression("${body.replace(\"O'C\", \"O C\")}", "Hello O Conner");
        assertExpression("${body.replace(\"O'C\", \"O-C\")}", "Hello O-Conner");
        assertExpression("${body.replace(\"O'C\", \"O''C\")}", "Hello O''Conner");
        assertExpression("${body.replace(\"O'C\", \"O\n'C\")}", "Hello O\n'Conner");
    }

    @Test
    public void testBodyOgnlSpaces() {
        exchange.getIn().setBody("Hello World");

        // no quotes, which is discouraged to use
        assertExpression("${body.compareTo(Hello World)}", 0);

        assertExpression("${body.compareTo('Hello World')}", 0);
        assertExpression("${body.compareTo(${body})}", 0);
        assertExpression("${body.compareTo('foo')}", "Hello World".compareTo("foo"));

        assertExpression("${body.compareTo( 'Hello World' )}", 0);
        assertExpression("${body.compareTo( ${body} )}", 0);
        assertExpression("${body.compareTo( 'foo' )}", "Hello World".compareTo("foo"));
    }

    @Test
    public void testClassSimpleName() {
        Animal tiger = new Animal("Tony the Tiger", 13);
        exchange.getIn().setBody(tiger);

        assertExpression("${body.getClass().getSimpleName()}", "Animal");
        assertExpression("${body.getClass.getSimpleName}", "Animal");
        assertExpression("${body.class.simpleName}", "Animal");
    }

    @Test
    public void testExceptionClassSimpleName() {
        Animal tiger = new Animal("Tony the Tiger", 13);
        exchange.getIn().setBody(tiger);

        Exception cause = new IllegalArgumentException("Forced");
        exchange.setException(cause);

        assertExpression("${exception.getClass().getSimpleName()}", "IllegalArgumentException");
        assertExpression("${exception.getClass.getSimpleName}", "IllegalArgumentException");
        assertExpression("${exception.class.simpleName}", "IllegalArgumentException");
    }

    @Test
    public void testSlashBeforeHeader() {
        assertExpression("foo/${header.foo}", "foo/abc");
        assertExpression("foo\\${header.foo}", "foo\\abc");
    }

    @Test
    public void testJSonLike() {
        exchange.getIn().setBody("Something");

        assertExpression("{\n\"data\": \"${body}\"\n}", "{\n\"data\": \"Something\"\n}");
    }

    @Test
    public void testFunctionEnds() {
        exchange.getIn().setBody("Something");

        assertExpression("{{", "{{");
        assertExpression("}}", "}}");
        assertExpression("{{}}", "{{}}");
        assertExpression("{{foo}}", "{{foo}}");
        assertExpression("{{${body}}}", "{{Something}}");
        assertExpression("{{${body}-${body}}}", "{{Something-Something}}");
    }

    @Test
    public void testEscape() {
        exchange.getIn().setBody("Something");

        // slash foo
        assertExpression("\\foo", "\\foo");

        assertExpression("\\n${body}", "\nSomething");
        assertExpression("\\t${body}", "\tSomething");
        assertExpression("\\r${body}", "\rSomething");
        assertExpression("\\n\\r${body}", "\n\rSomething");
        assertExpression("\\n${body}\\n", "\nSomething\n");
        assertExpression("\\t${body}\\t", "\tSomething\t");
        assertExpression("\\r${body}\\r", "\rSomething\r");
        assertExpression("\\n\\r${body}\\n\\r", "\n\rSomething\n\r");

        assertExpression("$${body}", "$Something");
    }

    @Test
    public void testEscapeEndFunction() {
        exchange.getIn().setBody("Something");

        assertExpression("{hello\\}", "{hello}");
        assertExpression("${body}{hello\\}", "Something{hello}");
    }

    @Test
    public void testCamelContextOGNL() {
        assertExpression("${camelContext.getName()}", context.getName());
        assertExpression("${camelContext.version}", context.getVersion());
    }

    @Test
    public void testTypeConstant() {
        assertExpression("${type:org.apache.camel.Exchange.FILE_NAME}", Exchange.FILE_NAME);
        assertExpression("${type:org.apache.camel.ExchangePattern.InOut}", ExchangePattern.InOut);

        // non existing fields
        Exception e1 = assertThrows(Exception.class,
                () -> assertExpression("${type:org.apache.camel.ExchangePattern.}", null),
                "Should throw exception");

        assertIsInstanceOf(ClassNotFoundException.class, e1.getCause());

        Exception e2 = assertThrows(Exception.class,
                () -> assertExpression("${type:org.apache.camel.ExchangePattern.UNKNOWN}", null),
                "Should throw exception");

        assertIsInstanceOf(ClassNotFoundException.class, e2.getCause());
    }

    @Test
    public void testTypeConstantInnerClass() {
        assertExpression("${type:org.apache.camel.language.simple.Constants$MyInnerStuff.FOO}", 123);
        assertExpression("${type:org.apache.camel.language.simple.Constants.BAR}", 456);
    }

    @Test
    public void testStringArrayLength() {
        exchange.getIn().setBody(new String[] { "foo", "bar" });
        assertExpression("${body[0]}", "foo");
        assertExpression("${body[1]}", "bar");
        assertExpression("${body.length}", 2);

        exchange.getIn().setBody(new String[] { "foo", "bar", "beer" });
        assertExpression("${body.length}", 3);
    }

    @Test
    public void testByteArrayLength() {
        exchange.getIn().setBody(new byte[] { 65, 66, 67 });
        assertExpression("${body[0]}", 65);
        assertExpression("${body[1]}", 66);
        assertExpression("${body[2]}", 67);
        assertExpression("${body.length}", 3);
    }

    @Test
    public void testIntArrayLength() {
        exchange.getIn().setBody(new int[] { 1, 20, 300 });
        assertExpression("${body[0]}", 1);
        assertExpression("${body[1]}", 20);
        assertExpression("${body[2]}", 300);
        assertExpression("${body.length}", 3);
    }

    @Test
    public void testSimpleMapBoolean() {
        Map<String, Object> map = new HashMap<>();
        exchange.getIn().setBody(map);

        map.put("isCredit", true);
        assertPredicate("${body[isCredit]} == true", true);
        assertPredicate("${body[isCredit]} == false", false);
        assertPredicate("${body['isCredit']} == true", true);
        assertPredicate("${body['isCredit']} == false", false);

        // wrong case
        assertPredicate("${body['IsCredit']} == true", false);

        map.put("isCredit", false);
        assertPredicate("${body[isCredit]} == true", false);
        assertPredicate("${body[isCredit]} == false", true);
        assertPredicate("${body['isCredit']} == true", false);
        assertPredicate("${body['isCredit']} == false", true);
    }

    @Test
    public void testSimpleRegexp() {
        exchange.getIn().setBody("12345678");
        assertPredicate("${body} regex '\\d+'", true);
        assertPredicate("${body} regex '\\w{1,4}'", false);

        exchange.getIn().setBody("tel:+97444549697");
        assertPredicate("${body} regex '^(tel:\\+)(974)(44)(\\d+)|^(974)(44)(\\d+)'", true);

        exchange.getIn().setBody("97444549697");
        assertPredicate("${body} regex '^(tel:\\+)(974)(44)(\\d+)|^(974)(44)(\\d+)'", true);

        exchange.getIn().setBody("tel:+87444549697");
        assertPredicate("${body} regex '^(tel:\\+)(974)(44)(\\d+)|^(974)(44)(\\d+)'", false);

        exchange.getIn().setBody("87444549697");
        assertPredicate("${body} regex '^(tel:\\+)(974)(44)(\\d+)|^(974)(44)(\\d+)'", false);
    }

    @Test
    public void testCollateEven() {
        List<Object> data = new ArrayList<>();
        data.add("A");
        data.add("B");
        data.add("C");
        data.add("D");
        data.add("E");
        data.add("F");
        exchange.getIn().setBody(data);

        Iterator it = (Iterator) evaluateExpression("${collate(3)}", null);
        List chunk = (List) it.next();
        List chunk2 = (List) it.next();
        assertFalse(it.hasNext());

        assertEquals(3, chunk.size());
        assertEquals(3, chunk2.size());

        assertEquals("A", chunk.get(0));
        assertEquals("B", chunk.get(1));
        assertEquals("C", chunk.get(2));
        assertEquals("D", chunk2.get(0));
        assertEquals("E", chunk2.get(1));
        assertEquals("F", chunk2.get(2));
    }

    @Test
    public void testCollateOdd() {
        List<Object> data = new ArrayList<>();
        data.add("A");
        data.add("B");
        data.add("C");
        data.add("D");
        data.add("E");
        data.add("F");
        data.add("G");
        exchange.getIn().setBody(data);

        Iterator it = (Iterator) evaluateExpression("${collate(3)}", null);
        List chunk = (List) it.next();
        List chunk2 = (List) it.next();
        List chunk3 = (List) it.next();
        assertFalse(it.hasNext());

        assertEquals(3, chunk.size());
        assertEquals(3, chunk2.size());
        assertEquals(1, chunk3.size());

        assertEquals("A", chunk.get(0));
        assertEquals("B", chunk.get(1));
        assertEquals("C", chunk.get(2));
        assertEquals("D", chunk2.get(0));
        assertEquals("E", chunk2.get(1));
        assertEquals("F", chunk2.get(2));
        assertEquals("G", chunk3.get(0));
    }

    @Test
    public void testJoinBody() {
        List<Object> data = new ArrayList<>();
        data.add("A");
        data.add("B");
        data.add("C");
        exchange.getIn().setBody(data);

        assertExpression("${join()}", "A,B,C");
        assertExpression("${join(;)}", "A;B;C");
        assertExpression("${join(' ')}", "A B C");
        assertExpression("${join(',','id=')}", "id=A,id=B,id=C");
        assertExpression("${join(&,id=)}", "id=A&id=B&id=C");
    }

    @Test
    public void testJoinHeader() {
        List<Object> data = new ArrayList<>();
        data.add("A");
        data.add("B");
        data.add("C");
        exchange.getIn().setHeader("id", data);

        assertExpression("${join('&','id=','${header.id}')}", "id=A&id=B&id=C");
    }

    @Test
    public void testRandomExpression() {
        int min = 1;
        int max = 10;
        int iterations = 30;
        int i = 0;
        for (i = 0; i < iterations; i++) {
            Expression expression = context.resolveLanguage("simple").createExpression("${random(1,10)}");
            assertTrue(
                    min <= expression.evaluate(exchange, Integer.class) && expression.evaluate(exchange, Integer.class) < max);
        }
        for (i = 0; i < iterations; i++) {
            Expression expression = context.resolveLanguage("simple").createExpression("${random(10)}");
            assertTrue(0 <= expression.evaluate(exchange, Integer.class) && expression.evaluate(exchange, Integer.class) < max);
        }
        Expression expression = context.resolveLanguage("simple").createExpression("${random(1, 10)}");
        assertTrue(min <= expression.evaluate(exchange, Integer.class) && expression.evaluate(exchange, Integer.class) < max);

        Expression expression1 = context.resolveLanguage("simple").createExpression("${random( 10)}");
        assertTrue(0 <= expression1.evaluate(exchange, Integer.class) && expression1.evaluate(exchange, Integer.class) < max);

        Exception e1 = assertThrows(Exception.class,
                () -> assertExpression("${random(10,21,30)}", null),
                "Should have thrown exception");

        assertEquals("Valid syntax: ${random(min,max)} or ${random(max)} was: random(10,21,30)", e1.getCause().getMessage());

        Exception e2 = assertThrows(Exception.class,
                () -> assertExpression("${random()}", null),
                "Should have thrown exception");

        assertEquals("Valid syntax: ${random(min,max)} or ${random(max)} was: random()", e2.getCause().getMessage());

        exchange.getIn().setHeader("max", 20);
        Expression expression3 = context.resolveLanguage("simple").createExpression("${random(10,${header.max})}");
        int num = expression3.evaluate(exchange, Integer.class);
        assertTrue(num >= 0 && num < 20, "Should be 10..20");
    }

    @Test
    public void testReplaceAllExpression() {
        exchange.getMessage().setBody("Hello a how are you");
        assertExpression("${replace(a,b)}", "Hello b how bre you");
        exchange.getMessage().setBody("{\"foo\": \"cheese\"}");
        assertExpression("${replace(&quot;,&apos;)}", "{'foo': 'cheese'}");
        exchange.getMessage().setBody("{'foo': 'cheese'}");
        assertExpression("${replace(&apos;,&quot;)}", "{\"foo\": \"cheese\"}");
        exchange.getMessage().setBody("{\"foo\": \"cheese\"}");
        assertExpression("${replace(&quot;,&empty;)}", "{foo: cheese}");

        exchange.getMessage().setBody("Hello");
        exchange.getMessage().setHeader("foo", "{\"foo\": \"cheese\"}");
        assertExpression("${replace(&quot;,&apos;,${header.foo})}", "{'foo': 'cheese'}");
    }

    @Test
    public void testList() {
        exchange.getMessage().setBody("4");
        assertExpression("${list(1,2,3)}", "[1, 2, 3]");
        assertExpression("${list(1,2,3,${body})}", "[1, 2, 3, 4]");
        assertExpression("${list('a','b','c')}", "[a, b, c]");
        assertExpression("${list()}", "[]");
    }

    @Test
    public void testMap() {
        exchange.getMessage().setBody("d");
        assertExpression("${map(1,a,2,b,3,c)}", "{1=a, 2=b, 3=c}");
        assertExpression("${map(1,a,2,b,3,c,4,${body})}", "{1=a, 2=b, 3=c, 4=d}");
        assertExpression("${map()}", "{}");
    }

    @Test
    public void testSubstringExpression() {
        exchange.getMessage().setBody("ABCDEFGHIJK");
        // head
        assertExpression("${substring(0)}", "ABCDEFGHIJK");
        assertExpression("${substring(1)}", "BCDEFGHIJK");
        assertExpression("${substring(3)}", "DEFGHIJK");
        assertExpression("${substring(99)}", "");
        // tail
        assertExpression("${substring(0)}", "ABCDEFGHIJK");
        assertExpression("${substring(-1)}", "ABCDEFGHIJ");
        assertExpression("${substring(-3)}", "ABCDEFGH");
        assertExpression("${substring(-99)}", "");
        // head and tail
        assertExpression("${substring(1,-1)}", "BCDEFGHIJ");
        assertExpression("${substring(3,-3)}", "DEFGH");
        assertExpression("${substring(1,-3)}", "BCDEFGH");
        assertExpression("${substring(3,-1)}", "DEFGHIJ");
        assertExpression("${substring(0,-1)}", "ABCDEFGHIJ");
        assertExpression("${substring(1,0)}", "BCDEFGHIJK");
        assertExpression("${substring(99,-99)}", "");
        assertExpression("${substring(0,-99)}", "");
        assertExpression("${substring(99,0)}", "");
        assertExpression("${substring(0,0)}", "ABCDEFGHIJK");

        exchange.getMessage().setBody("Hello World");
        exchange.getMessage().setHeader("foo", "1234567890");

        // head
        assertExpression("${substring(0,0,${header.foo})}", "1234567890");
        assertExpression("${substring(1,0,${header.foo})}", "234567890");
        assertExpression("${substring(3,0,${header.foo})}", "4567890");
        assertExpression("${substring(99,0,${header.foo})}", "");
        // tail
        assertExpression("${substring(0,0,${header.foo})}", "1234567890");
        assertExpression("${substring(0,-1,${header.foo})}", "123456789");
        assertExpression("${substring(0,-3,${header.foo})}", "1234567");
        assertExpression("${substring(0,-99,${header.foo})}", "");
        // head and tail
        assertExpression("${substring(1,-1,${header.foo})}", "23456789");
        assertExpression("${substring(3,-3,${header.foo})}", "4567");
        assertExpression("${substring(1,-3,${header.foo})}", "234567");
        assertExpression("${substring(3,-1,${header.foo})}", "456789");
        assertExpression("${substring(0,-1,${header.foo})}", "123456789");
        assertExpression("${substring(1,0,${header.foo})}", "234567890");
        assertExpression("${substring(99,-99,${header.foo})}", "");
        assertExpression("${substring(0,-99,${header.foo})}", "");
        assertExpression("${substring(99,0,${header.foo})}", "");
        assertExpression("${substring(0,0,${header.foo})}", "1234567890");
    }

    @Test
    public void testIif() {
        exchange.getIn().setHeader("foo", 44);
        assertExpression("${iif(${header.foo} > 0,positive,negative)}", "positive");
        exchange.getIn().setHeader("foo", -123);
        assertExpression("${iif(${header.foo} > 0,positive,negative)}", "negative");

        exchange.getIn().setBody("Hello World");
        exchange.getIn().setHeader("foo", 44);
        assertExpression("${iif(${header.foo} > 0,${body},Bye World)}", "Hello World");
        exchange.getIn().setHeader("foo", -123);
        assertExpression("${iif(${header.foo} > 0,${body},Bye World)}", "Bye World");
        assertExpression("${iif(${header.foo} > 0,${body},${null})}", null);

        exchange.getIn().setHeader("CamelFileName", "testfile.txt");
        assertExpression("${iif(${file:name} startsWith 'test',foo,bar)}", "foo");
        exchange.getIn().setHeader("CamelFileName", "dummy.txt");
        assertExpression("${iif(${file:name} startsWith 'test',foo,bar)}", "bar");
    }

    @Test
    public void testListRemoveByInstance() {
        List<Object> data = new ArrayList<>();
        data.add("A");
        data.add("B");
        exchange.getIn().setBody(data);

        assertEquals(2, data.size());

        Expression expression = context.resolveLanguage("simple").createExpression("${body.remove('A')}");
        expression.evaluate(exchange, Object.class);

        assertEquals(1, data.size());
        assertEquals("B", data.get(0));
    }

    @Test
    public void testListRemoveIndex() {
        List<Object> data = new ArrayList<>();
        data.add("A");
        data.add("B");
        exchange.getIn().setBody(data);

        assertEquals(2, data.size());

        Expression expression = context.resolveLanguage("simple").createExpression("${body.remove(0)}");
        expression.evaluate(exchange, Object.class);

        assertEquals(1, data.size());
        assertEquals("B", data.get(0));
    }

    @Test
    public void testBodyOgnlOnAnimalWithOgnlParams() {
        exchange.getIn().setBody(new Animal("tiger", 13));
        exchange.getIn().setHeader("friend", new Animal("donkey", 4));
        assertExpression("${body.setFriend(${header.friend})}", null);

        Animal animal = exchange.getIn().getBody(Animal.class);
        assertEquals("tiger", animal.getName());
        assertEquals(13, animal.getAge());
        assertNotNull(animal.getFriend(), "Should have a friend");
        assertEquals("donkey", animal.getFriend().getName());
        assertEquals(4, animal.getFriend().getAge());
    }

    @Test
    public void testBodyAsOneLine() {
        exchange.getIn().setBody("Hello" + System.lineSeparator() + "Great" + System.lineSeparator() + "World");
        assertExpression("${bodyOneLine}", "HelloGreatWorld");
        assertExpression("Hi ${bodyOneLine}", "Hi HelloGreatWorld");
        assertExpression("Hi ${bodyOneLine} Again", "Hi HelloGreatWorld Again");
    }

    @Test
    public void testJsonPrettyPrint() {

        StringBuilder expectedJson = new StringBuilder();
        expectedJson.append("{");
        expectedJson.append("\n");
        expectedJson.append("\t\"firstName\": \"foo\",");
        expectedJson.append("\n");
        expectedJson.append("\t\"lastName\": \"bar\"");
        expectedJson.append("\n");
        expectedJson.append("}");
        expectedJson.append("\n");

        exchange.getIn().setBody("{\"firstName\": \"foo\", \"lastName\": \"bar\"}");
        assertExpression("${prettyBody}", expectedJson.toString());
        assertExpression("Hi ${prettyBody}", "Hi " + expectedJson);
        assertExpression("Hi ${prettyBody} Again", "Hi " + expectedJson + " Again");

        expectedJson = new StringBuilder();
        expectedJson.append("[");
        expectedJson.append("\n");
        expectedJson.append("\t{");
        expectedJson.append("\n");
        expectedJson.append("\t\t\"firstName\": \"foo\",");
        expectedJson.append("\n");
        expectedJson.append("\t\t\"lastName\": \"bar\"");
        expectedJson.append("\n");
        expectedJson.append("\t},");
        expectedJson.append("\n");
        expectedJson.append("\t{");
        expectedJson.append("\n");
        expectedJson.append("\t\t\"firstName\": \"foo\",");
        expectedJson.append("\n");
        expectedJson.append("\t\t\"lastName\": \"bar\"");
        expectedJson.append("\n");
        expectedJson.append("\t}");
        expectedJson.append("\n");
        expectedJson.append("]");
        expectedJson.append("\n");

        exchange.getIn()
                .setBody("[{\"firstName\": \"foo\", \"lastName\": \"bar\"},{\"firstName\": \"foo\", \"lastName\": \"bar\"}]");
        assertExpression("${prettyBody}", expectedJson.toString());
        assertExpression("Hi ${prettyBody}", "Hi " + expectedJson);
        assertExpression("Hi ${prettyBody} Again", "Hi " + expectedJson + " Again");

    }

    @Test
    public void testXMLPrettyPrint() {
        StringBuilder expectedXml = new StringBuilder();
        expectedXml.append("<person>");
        expectedXml.append("\n");
        expectedXml.append("  <firstName>");
        expectedXml.append("\n");
        expectedXml.append("    foo");
        expectedXml.append("\n");
        expectedXml.append("  </firstName>");
        expectedXml.append("\n");
        expectedXml.append("  <lastName>");
        expectedXml.append("\n");
        expectedXml.append("    bar");
        expectedXml.append("\n");
        expectedXml.append("  </lastName>");
        expectedXml.append("\n");
        expectedXml.append("</person>");

        exchange.getIn().setBody("<person><firstName>foo</firstName><lastName>bar</lastName></person>");

        assertExpression("${prettyBody}", expectedXml.toString());
        assertExpression("Hi ${prettyBody}", "Hi " + expectedXml);
        assertExpression("Hi ${prettyBody} Again", "Hi " + expectedXml + " Again");
    }

    @Test
    public void testNestedTypeFunction() {
        // when using type: function we need special logic to not lazy evaluate
        // it so its evaluated only once
        // and won't fool Camel to think its a nested OGNL method call
        // expression instead (CAMEL-10664)
        exchange.setProperty(Exchange.AUTHENTICATION, 123);
        String exp = "${exchangeProperty.${type:org.apache.camel.Exchange.AUTHENTICATION}.toString()}";
        assertExpression(exp, "123");

        exchange.getIn().setHeader("whichOne", "AUTHENTICATION");
        exchange.setProperty(Exchange.AUTHENTICATION, 456);
        exp = "${exchangeProperty.${type:org.apache.camel.Exchange.${header.whichOne}}.toString()}";
        assertExpression(exp, "456");
    }

    @Test
    public void testListIndexByNestedFunction() {
        List<String> alist = new ArrayList<>();
        alist.add("1");
        alist.add("99");
        exchange.getIn().setHeader("ITEMS", alist);
        exchange.getIn().setHeader("TOTAL_LOOPS", alist.size());

        String exp = "${header.ITEMS[${exchangeProperty.CamelLoopIndex}]}";

        exchange.setProperty(Exchange.LOOP_INDEX, 0);
        assertExpression(exp, "1");
        exchange.setProperty(Exchange.LOOP_INDEX, 1);
        assertExpression(exp, "99");
    }

    @Test
    public void testMessageTimestamp() {
        exchange.getIn().setHeader(Exchange.MESSAGE_TIMESTAMP, 1234L);
        assertExpression("${messageTimestamp}", 1234L);
    }

    @Test
    public void testParenthesisReplaceAll() {
        exchange.getIn().setBody("Bik (Ru)");
        assertExpression("${body.replaceAll(\"Bik \\(Ru\\)\",\"bik_ru\").replaceAll(\"b\",\"c\")}", "cik_ru");
    }

    @Test
    public void testParenthesisReplace() {
        exchange.getIn().setBody("Hello (( World (((( Again");
        assertExpression("${body.replace(\"((\", \"--\").replace(\"((((\", \"----\")}", "Hello -- World ---- Again");
    }

    @Test
    public void testPropertiesExist() {
        PropertiesComponent pc = context.getPropertiesComponent();

        assertExpression("${propertiesExist:myKey}", "false");
        assertExpression("${propertiesExist:!myKey}", "true");
        assertPredicate("${propertiesExist:myKey}", false);
        assertPredicate("${propertiesExist:!myKey}", true);

        pc.addInitialProperty("myKey", "abc");
        assertExpression("${propertiesExist:myKey}", "true");
        assertExpression("${propertiesExist:!myKey}", "false");
        assertPredicate("${propertiesExist:myKey}", true);
        assertPredicate("${propertiesExist:!myKey}", false);
    }

    @Test
    public void testUuid() {
        Expression expression = context.resolveLanguage("simple").createExpression("${uuid}");
        String s = expression.evaluate(exchange, String.class);
        assertNotNull(s);

        expression = context.resolveLanguage("simple").createExpression("${uuid(default)}");
        s = expression.evaluate(exchange, String.class);
        assertNotNull(s);

        expression = context.resolveLanguage("simple").createExpression("${uuid(short)}");
        s = expression.evaluate(exchange, String.class);
        assertNotNull(s);

        expression = context.resolveLanguage("simple").createExpression("${uuid(simple)}");
        s = expression.evaluate(exchange, String.class);
        assertNotNull(s);

        expression = context.resolveLanguage("simple").createExpression("${uuid(classic)}");
        s = expression.evaluate(exchange, String.class);
        assertNotNull(s);

        // custom generator
        context.getRegistry().bind("mygen", (UuidGenerator) () -> "1234");
        assertExpression("${uuid(mygen)}", "1234");
    }

    @Test
    public void testHash() throws Exception {
        Expression expression = context.resolveLanguage("simple").createExpression("${hash(hello)}");
        String s = expression.evaluate(exchange, String.class);
        assertNotNull(s);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest("hello".getBytes(StandardCharsets.UTF_8));
        String expected = StringHelper.bytesToHex(bytes);
        assertEquals(expected, s);

        expression = context.resolveLanguage("simple").createExpression("${hash(${body})}");
        s = expression.evaluate(exchange, String.class);
        assertNotNull(s);
        digest = MessageDigest.getInstance("SHA-256");
        bytes = digest.digest(exchange.getMessage().getBody(String.class).getBytes(StandardCharsets.UTF_8));
        expected = StringHelper.bytesToHex(bytes);
        assertEquals(expected, s);

        expression = context.resolveLanguage("simple").createExpression("${hash(${header.foo})}");
        s = expression.evaluate(exchange, String.class);
        assertNotNull(s);

        expression = context.resolveLanguage("simple").createExpression("${hash(hello,SHA3-256)}");
        s = expression.evaluate(exchange, String.class);
        assertNotNull(s);

        expression = context.resolveLanguage("simple").createExpression("${hash(${body},SHA3-256)}");
        s = expression.evaluate(exchange, String.class);
        assertNotNull(s);
        digest = MessageDigest.getInstance("SHA3-256");
        bytes = digest.digest(exchange.getMessage().getBody(String.class).getBytes(StandardCharsets.UTF_8));
        expected = StringHelper.bytesToHex(bytes);
        assertEquals(expected, s);

        expression = context.resolveLanguage("simple").createExpression("${hash(${header.foo},SHA3-256)}");
        s = expression.evaluate(exchange, String.class);
        assertNotNull(s);

        expression = context.resolveLanguage("simple").createExpression("${hash(${header.unknown})}");
        s = expression.evaluate(exchange, String.class);
        assertNull(s);
    }

    @Test
    public void testNewEmpty() {
        assertExpressionCreateNewEmpty("list", List.class, v -> ((List) v).isEmpty());
        assertExpressionCreateNewEmpty("LIST", List.class, v -> ((List) v).isEmpty());
        assertExpressionCreateNewEmpty("List", List.class, v -> ((List) v).isEmpty());
        assertExpressionCreateNewEmpty("map", Map.class, v -> ((Map) v).isEmpty());
        assertExpressionCreateNewEmpty("MAP", Map.class, v -> ((Map) v).isEmpty());
        assertExpressionCreateNewEmpty("Map", Map.class, v -> ((Map) v).isEmpty());
        assertExpressionCreateNewEmpty("string", String.class, v -> ((String) v).isEmpty());
        assertExpressionCreateNewEmpty("STRING", String.class, v -> ((String) v).isEmpty());
        assertExpressionCreateNewEmpty("String", String.class, v -> ((String) v).isEmpty());

        assertThrows(SimpleIllegalSyntaxException.class, () -> evaluateExpression("${empty(falseSyntax}", null));
        assertThrows(SimpleIllegalSyntaxException.class, () -> evaluateExpression("${empty()}", null));
        assertThrows(SimpleIllegalSyntaxException.class, () -> evaluateExpression("${empty(}", null));
        assertThrows(SimpleIllegalSyntaxException.class, () -> evaluateExpression("${empty}", null));
        assertThrows(IllegalArgumentException.class, () -> evaluateExpression("${empty(unknownType)}", null));
    }

    @Test
    public void testPretty() {
        assertExpression(exchange, "${pretty('Hello')}", "Hello");
        assertExpression(exchange, "${pretty(${body})}", "<hello id=\"m123\">\n</hello>");

        exchange.getMessage().setBody("{\"name\": \"Jack\", \"id\": 123}");
        assertExpression(exchange, "${pretty(${body})}", "{\n\t\"name\": \"Jack\",\n\t\"id\": 123\n}\n");
    }

    private void assertExpressionCreateNewEmpty(
            String type, Class<?> expectedClass, java.util.function.Predicate<Object> isEmptyAssertion) {
        Object value = evaluateExpression("${empty(%s)}".formatted(type), null);
        assertNotNull(value);
        assertIsInstanceOf(expectedClass, value);
        assertTrue(isEmptyAssertion.test(value));
    }

    @Override
    protected String getLanguageName() {
        return "simple";
    }

    protected void assertExpressionResultInstanceOf(String expressionText, Class<?> expectedType) {
        Language language = assertResolveLanguage(getLanguageName());
        Expression expression = language.createExpression(expressionText);
        assertNotNull(expectedType, "Cannot assert type when no type is provided");
        assertNotNull(expression, "No Expression could be created for text: " + expressionText + " language: " + language);
        Object answer = expression.evaluate(exchange, Object.class);
        assertIsInstanceOf(expectedType, answer);
    }

    public static final class Animal {
        private final String name;
        private final int age;
        private Animal friend;

        private Animal(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public Animal getFriend() {
            return friend;
        }

        public void setFriend(Animal friend) {
            this.friend = friend;
        }

        public boolean isDangerous() {
            return name.contains("Tiger");
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final class Order {
        private List<OrderLine> lines;

        public Order(List<OrderLine> lines) {
            this.lines = lines;
        }

        public List<OrderLine> getLines() {
            return lines;
        }

        public void setLines(List<OrderLine> lines) {
            this.lines = lines;
        }
    }

    public static final class OrderLine {
        private final int id;
        private final String name;

        public OrderLine(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class MyClass {
        public Object[] getMyArray() {
            return new Object[] { "Hallo", "World", "!" };
        }
    }

}
