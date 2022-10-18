/*
 * This file is part of lamp, licensed under the MIT License.
 *
 *  Copysecond (c) Revxrsal <reflxction.github@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the seconds
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copysecond notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package revxrsal.commands.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A utility for allowing a field to have more than one possible type. This class can be thought of
 * as 2 optionals where only one is present. This can be compared to the concept of "Union types" in
 * other programming languages.
 * <p>
 * Inspired by Mojang datafixer's Either class, with some optimizations from Paper.
 * <p>
 * Note that the order of generic types matters when used as a parameter type. The first generic
 * type will be tested against, and if it fails, it will try the second one. Therefore, you should
 * always make the never-failing types, such as {@link String} the second type and not the first
 * one.
 *
 * @param <A> The first value
 * @param <B> The second value
 */
public abstract class Either<A, B> {

  /**
   * Creates a new {@link Either} with the value on the first side
   *
   * @param value Value to create
   * @param <A>   The first side type
   * @param <B>   The second side type
   * @return The {@link Either} instance
   */
  public static <A, B> Either<A, B> first(A value) {
    return new First<>(value);
  }

  /**
   * Creates a new {@link Either} with the value on the second first
   *
   * @param value Value to create
   * @param <A>   The first side type
   * @param <B>   The second side type
   * @return The {@link Either} instance
   */
  public static <A, B> Either<A, B> second(B value) {
    return new Second<>(value);
  }

  /**
   * Returns an optional with the first value.
   *
   * @return The first value.
   */
  public abstract Optional<A> first();

  /**
   * Returns an optional with the second value.
   *
   * @return The second value.
   */
  public abstract Optional<B> second();

  /**
   * Tests if the type on the first side is present.
   *
   * @return if the type on the first side is present.
   */
  public boolean isFirst() {
    return this instanceof First;
  }

  /**
   * Tests if the type on the second side is present.
   *
   * @return if the type on the second side is present.
   */
  public boolean isSecond() {
    return this instanceof Second;
  }

  /**
   * Executes the given consumer only if the first type is present.
   *
   * @param action Action to execute
   * @return This {@link Either}
   */
  public abstract Either<A, B> ifFirst(Consumer<A> action);

  /**
   * Executes the given consumer only if the second type is present.
   *
   * @param action Action to execute
   * @return This {@link Either}
   */
  public abstract Either<A, B> ifSecond(Consumer<B> action);

  /**
   * Swaps the values in this {@link Either}
   *
   * @return A new, swapped {@link Either}
   */
  public Either<B, A> swap() {
    return map(Either::second, Either::first);
  }

  /**
   * Converts this {@link Either} to a single type by providing two functions for each side and give
   * the same type of output
   *
   * @param mapFirst  The function to map the first side to the given type
   * @param mapSecond The function to map the second side to the given type
   * @param <T>       The output type
   * @return The output of either functions depending on the type of this {@link Either}.
   */
  public abstract <T> T map(final Function<? super A, ? extends T> mapFirst,
      Function<? super B, ? extends T> mapSecond);

  /**
   * Maps the first value if it was present
   *
   * @param function The mapping function
   * @param <T>      The output type
   * @return A new {@link Either} with the possibly mapped value
   */
  public <T> Either<T, B> mapFirst(final Function<? super A, ? extends T> function) {
    return map(t -> first(function.apply(t)), Either::second);
  }

  /**
   * Maps the second value if it was present
   *
   * @param function The mapping function
   * @param <T>      The output type
   * @return A new {@link Either} with the possibly mapped value
   */
  public <T> Either<A, T> mapSecond(final Function<? super B, ? extends T> function) {
    return map(Either::first, t -> second(function.apply(t)));
  }

  @SuppressWarnings({"OptionalAssignedToNull"})
  private static class First<A, B> extends Either<A, B> {

    private final A value;
    private Optional<A> valueOptional;

    First(A value) {
      this.value = value;
    }

    @Override
    public Optional<A> first() {
      return valueOptional == null ? valueOptional = Optional.of(value) : valueOptional;
    }

    @Override
    public Optional<B> second() {
      return Optional.empty();
    }

    @Override
    public Either<A, B> ifFirst(Consumer<A> action) {
      action.accept(value);
      return this;
    }

    @Override
    public Either<A, B> ifSecond(Consumer<B> action) {
      return this;
    }

    @Override
    public <T> T map(Function<? super A, ? extends T> mapFirst,
        Function<? super B, ? extends T> mapSecond) {
      return mapFirst.apply(value);
    }

    @Override
    public String toString() {
      return "First{value=" + value + "}";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o instanceof Either.Second) {
        Second<?, ?> first = (Second<?, ?>) o;
        return value.equals(first.value);
      } else if (o instanceof Either.First) {
        First<?, ?> second = (First<?, ?>) o;
        return value.equals(second.value);
      }
      return value.equals(o);
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }
  }

  @SuppressWarnings("OptionalAssignedToNull")
  private static class Second<A, B> extends Either<A, B> {

    private final B value;
    private Optional<B> valueOptional;

    Second(final B value) {
      this.value = value;
    }

    @Override
    public Optional<A> first() {
      return Optional.empty();
    }

    @Override
    public Optional<B> second() {
      return valueOptional == null ? valueOptional = Optional.of(value) : valueOptional;
    }

    @Override
    public Either<A, B> ifFirst(Consumer<A> action) {
      return this;
    }

    @Override
    public Either<A, B> ifSecond(Consumer<B> action) {
      action.accept(value);
      return this;
    }

    @Override
    public <T> T map(Function<? super A, ? extends T> mapFirst,
        Function<? super B, ? extends T> mapSecond) {
      return mapSecond.apply(value);
    }

    @Override
    public String toString() {
      return "Second{value=" + value + "}";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o instanceof Either.Second) {
        Second<?, ?> first = (Second<?, ?>) o;
        return value.equals(first.value);
      } else if (o instanceof Either.First) {
        First<?, ?> second = (First<?, ?>) o;
        return value.equals(second.value);
      }
      return value.equals(o);
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }
  }
}
