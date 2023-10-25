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

import java.time.Duration;

import java.util.function.Supplier;

/**
 * A utility class containing useful operations for {@link Supplier}s.
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 */
public final class Suppliers {

  /**
   * Creates a new {@link Suppliers}.
   */
  private Suppliers() {
    super();
  }

  /**
   * <a href="https://en.wikipedia.org/wiki/Memoization" target="_top"><em>Memoizes</em></a> the supplied {@link
   * Supplier} and returns the memoization.
   *
   * <p>The memoization will not call the supplied {@link Supplier}'s {@link Supplier#get() get()} method until its own
   * {@link Supplier#get() get()} method is called.</p>
   *
   * @param <R> the return type of the supplied {@link Supplier}
   *
   * @param s the {@link Supplier} to memoize; must not be {@code null}
   *
   * @return a memoized version of the supplied {@link Supplier}; never {@code null}
   *
   * @exception NullPointerException if {@code s} is {@code null}
   */
  @SuppressWarnings("unchecked")
  public static final <R> Supplier<R> memoize(final Supplier<? extends R> s) {
    if (s.getClass().getEnclosingClass() == Suppliers.class && s.getClass().isAnonymousClass()) {
      // Already memoized.
      return (Supplier<R>)s;
    }
    return new Supplier<>() {
      private Supplier<R> d = this::compute; // deliberately not final; no need for volatile; construction semantics
      private boolean initialized; // deliberately not final; no need for volatile; always accessed under lock
      private final synchronized R compute() {
        if (!this.initialized) {
          final R r = s.get();
          this.d = () -> r; // memoization
          this.initialized = true;
        }
        return this.d.get();
      }
      @Override
      public final R get() {
        return this.d.get(); // no need for volatile; d is either synchronized or "immutable"
      }
    };
  }

  /**
   * <a href="https://en.wikipedia.org/wiki/Memoization" target="_top"><em>Memoizes</em></a> the supplied {@link
   * Supplier} for a time equal to that represented by the supplied {@link Duration} and returns the memoization.
   *
   * <p>The memoization will not call the supplied {@link Supplier}'s {@link Supplier#get() get()} method until its own
   * {@link Supplier#get() get()} method is called.</p>
   *
   * @param <R> the return type of the supplied {@link Supplier}
   *
   * @param s the {@link Supplier} to memoize; must not be {@code null}
   *
   * @param d the {@link Duration} expressing how long the memoization will survive; must not be {@code null}
   *
   * @return a memoized version of the supplied {@link Supplier}; never {@code null}
   *
   * @exception NullPointerException if either argument is {@code null}
   *
   * @see #memoize(Supplier, long)
   */
  public static final <R> Supplier<R> memoize(final Supplier<? extends R> s, final Duration d) {
    return memoize(s, d.toNanos());
  }

  /**
   * <a href="https://en.wikipedia.org/wiki/Memoization" target="_top"><em>Memoizes</em></a> the supplied {@link
   * Supplier} for a time equal to that represented by the supplied number of nanoseconds and returns the memoization.
   *
   * <p>The memoization will not call the supplied {@link Supplier}'s {@link Supplier#get() get()} method until its own
   * {@link Supplier#get() get()} method is called.</p>
   *
   * @param <R> the return type of the supplied {@link Supplier}
   *
   * @param s the {@link Supplier} to memoize; must not be {@code null}
   *
   * @param durationNanos the duration, in nanoseconds, expressing how long the memoization will survive
   *
   * @return a memoized version of the supplied {@link Supplier}; never {@code null}
   *
   * @exception NullPointerException if either argument is {@code null}
   */
  public static final <R> Supplier<R> memoize(final Supplier<? extends R> s, final long durationNanos) {
    return new Supplier<>() {
      private volatile long expiresAt;
      private volatile R r;
      @Override
      public final R get() {
        final long expiresAt = this.expiresAt; // volatile read
        final long now = System.nanoTime();
        if (expiresAt == 0L || now - expiresAt >= 0L) {
          // first time or expiration
          synchronized (this) {
            // double-checked locking
            if (expiresAt == this.expiresAt) { // volatile read
              this.r = s.get(); // volatile write; memoization
              this.expiresAt = now + durationNanos; // volatile write
            }
          }
        }
        return this.r; // volatile read
      }
    };
  }

}
