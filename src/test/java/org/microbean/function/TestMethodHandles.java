/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2023 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.microbean.function;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static java.lang.invoke.MethodHandles.empty;
import static java.lang.invoke.MethodHandles.filterArguments;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class TestMethodHandles {

  private TestMethodHandles() {
    super();
  }

  @Test
  final void testBindingToSupplier() throws Throwable {
    final Lookup lookup = lookup();

    final MethodHandle unboundToString = lookup.findVirtual(Object.class, "toString", methodType(String.class));
    assertEquals(1, unboundToString.type().parameterCount()); // 1 for the receiver
    assertSame(Object.class, unboundToString.type().parameterType(0));
    assertSame(String.class, unboundToString.type().returnType());

    final Integer three = Integer.valueOf(3);
    final MethodHandle threeToString = unboundToString.bindTo(three);
    assertEquals("3", (String)threeToString.invokeExact());

    final Supplier<?> s = new Supplier<>() {
        private int x = -1;
        @Override
        public final Integer get() {
          return ++x;
        }
      };

    final MethodHandle filter =
      insertArguments(lookup.findStatic(TestMethodHandles.class,
                                        "receiver",
                                        methodType(Object.class,
                                                   Object.class,
                                                   Supplier.class)),
                      1,
                      s);
    assertEquals(1, filter.type().parameterCount());

    final MethodHandle toStringBoundToSupplier = filterArguments(unboundToString, 0, filter).bindTo(null); // bind is totally arbitrary
    assertEquals("0", (String)toStringBoundToSupplier.invokeExact());
    assertEquals("1", (String)toStringBoundToSupplier.invokeExact());
    
  }

  private static final Object receiver(final Object ignored, final Supplier<?> s) {
    return s.get();
  }

      
  
}
