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
package revxrsal.commands.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import revxrsal.commands.command.CommandParameter;

/**
 * Adds a default value for a parameter when it is not supplied.
 * <p>
 * Due to limitations and simply the lots of "edge cases", this parameter only works if there are no
 * parameters after it, or if all following parameters are also marked with {@link Default}.
 * <p>
 * Note that if any parameter is annotated with {@link Default}, it will automatically be marked as
 * optional.
 * <p>
 * Accessible with {@link CommandParameter#getDefaultValue()}.
 * <p>
 * Aside from being used on parameters, this annotation can also be used on methods that should be
 * invoked when an invalid command is inputted in the command tree. Methods annotated with
 * {@link Default} can have parameters just like any other command methods.
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Default {

  /**
   * The default value. This will be passed to resolvers just as if the user inputted it.
   * <p>
   * If none is specified, and the argument is not present, {@code null} will be passed to the
   * command.
   *
   * @return The parameter's default value. Multiple strings indicate more than 1 argument.
   * Supplying an empty array will be ignored
   */
  String[] value() default {};

}
