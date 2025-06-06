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
package org.apache.camel.util.function;

/**
 * Represents a function that accepts a single arguments, produces a result and may throw an exception.
 *
 * @param <I> the type of the input of the function
 * @param <R> the type of the result of the function
 * @param <T> the type of the exception the accept method may throw
 *
 * @see       java.util.function.Function
 */
@FunctionalInterface
public interface ThrowingFunction<I, R, T extends Throwable> {
    /**
     * Applies this function to the given argument, potentially throwing an exception.
     *
     * @param  in the function argument
     * @return    the function result
     * @throws T  the exception that may be thrown
     */
    R apply(I in) throws T;
}
