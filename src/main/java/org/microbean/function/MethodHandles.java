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

import java.util.function.Supplier;

import java.lang.invoke.MethodHandle;

import static java.lang.invoke.MethodHandles.foldArguments;

import static java.lang.invoke.MethodType.methodType;

/**
 * A utility class for working with {@linkplain MethodHandle method handles}.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 */
public final class MethodHandles {


  /*
   * Static fields and initializers.
   */


  /**
   * A direct {@link MethodHandle} representing {@link Supplier#get()}.
   *
   * <p>This field is never {@code null}.</p>
   */
  private static final MethodHandle unboundSupplierGet;

  static {
    try {
      unboundSupplierGet = java.lang.invoke.MethodHandles.lookup().findVirtual(Supplier.class, "get", methodType(Object.class));
    } catch (IllegalAccessException | NoSuchMethodException e) {
      throw new ExceptionInInitializerError(e);
    }
  }


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link MethodHandles}.
   */
  private MethodHandles() {
    super();
  }


  /*
   * Static methods.
   */


  /**
   * Returns a {@link MethodHandle} adapter that, with each invocation, invokes the supplied {@link MethodHandle} with a
   * leading argument supplied by an invocation of the supplied {@link Supplier}'s {@link Supplier#get() get()} method.
   *
   * <p>Normally the supplied {@link MethodHandle} should represent a virtual method whose receiver has not yet been
   * {@linkplain MethodHandle#bindTo(Object) bound}.</p>
   *
   * @param mh a {@link MethodHandle} that has at least one parameter of type {@link Object};
   * must not be {@code null}
   *
   * @param s a {@link Supplier}; must not be {@code null}
   *
   * @return a {@link MethodHandle} adapter that, with each invocation, invokes the supplied {@link MethodHandle} with a
   * leading argument supplied by an invocation of the supplied {@link Supplier}'s {@link Supplier#get() get()} method
   *
   * @see java.lang.invoke.MethodHandles#foldArguments(MethodHandle, MethodHandle)
   */
  public static final MethodHandle bindTo(final MethodHandle mh, final Supplier<?> s) {
    return foldArguments(mh, unboundSupplierGet.bindTo(s));
  }

}
