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
import static java.lang.invoke.MethodHandles.foldArguments;
import static java.lang.invoke.MethodHandles.identity;
import static java.lang.invoke.MethodHandles.insertArguments;
import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class TestMethodHandles {

  private static final Lookup lookup = lookup();
  
  private static final MethodHandle receiver0;

  private static final MethodHandle unboundToString;

  private static final MethodHandle unboundSupplierGet;

  static {
    try {
      unboundToString = lookup.findVirtual(Object.class, "toString", methodType(String.class));
      unboundSupplierGet = lookup.findVirtual(Supplier.class, "get", methodType(Object.class));
      receiver0 = lookup.findStatic(TestMethodHandles.class, "receiver0", methodType(Object.class, Object.class, Supplier.class));
    } catch (IllegalAccessException | NoSuchMethodException e) {
      throw new ExceptionInInitializerError(e);
    }
  }
  
  private TestMethodHandles() {
    super();
  }

  @Test
  final void testOrdinaryBinding() throws Throwable {
    final Integer three = Integer.valueOf(3);
    final MethodHandle threeToString = unboundToString.bindTo(three);
    assertEquals("3", (String)threeToString.invokeExact());
  }
  
  @Test
  final void testBindingToSupplier0() throws Throwable {
    final Supplier<?> s = new Supplier<>() {
        private int x = -1;
        @Override
        public final Integer get() {
          return ++x;
        }
      };

    // Turn receiver(Object, Supplier) into simply receiver(Object), closing over s
    final MethodHandle filter = insertArguments(receiver0, 1, s);
    assertEquals(1, filter.type().parameterCount());
    assertSame(Object.class, filter.type().parameterType(0));

    // Now we can use the filter to replace the 0th argument (receiver) to unboundToString.  First we take the
    // unboundToString method handle, which, because it is unbound, accepts one parameter (the receiver; toString() is a
    // virtual method). We filter its zeroth argument (the receiver) to be the return value of the filter method handle
    // we just built. This effectively "binds" toString() to s.get() on every invocation.
    //
    // We dummy bind the unboundToString method handle to null. The filter will replace that zeroth argument with its
    // return value (Supplier#get()).
    final MethodHandle toStringBoundToSupplier = filterArguments(unboundToString, 0, filter)
      .bindTo(null); // arbitrary; will be replaced by return value of filter
    assertEquals("0", (String)toStringBoundToSupplier.invokeExact());
    assertEquals("1", (String)toStringBoundToSupplier.invokeExact());
    
  }

  @Test
  final void testBindingToSupplier1() throws Throwable {
    final Supplier<?> s = new Supplier<>() {
        private int x = -1;
        @Override
        public final Integer get() {
          return ++x;
        }
      };

    final MethodHandle mh = foldArguments(unboundToString, unboundSupplierGet.bindTo(s));
    assertEquals("0", (String)mh.invokeExact());
    assertEquals("1", (String)mh.invokeExact());
  }

  private static final Object receiver0(final Object ignored, final Supplier<?> s) {
    return s.get();
  }

}
