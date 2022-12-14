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
package revxrsal.commands.process;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.command.CommandParameter;
import revxrsal.commands.exception.CommandExceptionHandler;

/**
 * A validator for a specific parameter type. These validators can do extra checks on parameters
 * after they are resolved from {@link ValueResolver} or {@link ContextResolver}s.
 * <p>
 * Validators work on subclasses as well. For example, we can write a validator to validate a
 * custom
 * <code>@Range(min, max)</code> annotation for numbers:
 *
 * <pre>{@code
 * public enum RangeValidator implements ParameterValidator<Number> {
 *     INSTANCE;
 *
 *     @Override public void validate(Number value, @NotNull CommandParameter parameter, @NotNull CommandActor actor) throws Throwable {
 *         Range range = parameter.getAnnotation(Range.class);
 *         if (range == null) return;
 *         double d = value.doubleValue();
 *         if (d < range.min())
 *             throw new CommandErrorException(actor, "Specified value (" + d + ") is less than minimum " + range.min());
 *         if (d > range.max())
 *             throw new CommandErrorException(actor, "Specified value (" + d + ") is greater than maximum " + range.max());
 *     }
 * }
 * }</pre>
 * <p>
 * These can be registered through
 * {@link CommandHandler#registerParameterValidator(Class, ParameterValidator)}
 *
 * @param <T> The parameter handler
 */
public interface ParameterValidator<T> {

  /**
   * Validates the specified value that was passed to a parameter.
   * <p>
   * Ideally, a validator will want to throw an exception when the parameter is not valid, and then
   * further handled with {@link CommandExceptionHandler}.
   *
   * @param value     The parameter value. May or may not be null, depending on the resolver.
   * @param parameter The parameter that will take this value
   * @param actor     The command actor
   */
  void validate(T value, @NotNull CommandParameter parameter, @NotNull CommandActor actor);

}
