package dev.demeng.pluginbase.commands.util;

import static dev.demeng.pluginbase.commands.util.Collections.listOf;
import static java.util.Collections.emptyList;

import dev.demeng.pluginbase.commands.core.BaseCommandDispatcher;
import dev.demeng.pluginbase.commands.core.reflect.MethodCaller;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * A utility for stripping stacktraces from local paths to classes. This helps keep errors clean as
 * well as removing unnecessary trace paths which do not help.
 */
public final class StackTraceSanitizer {

  /**
   * The default stack trace sanitizer
   */
  private static final StackTraceSanitizer DEFAULT_SANITIZER = StackTraceSanitizer.builder()
      .ignoreClasses(BaseCommandDispatcher.class)
      .ignoreClasses(MethodHandles.class, MethodHandle.class)
      .ignorePackage(MethodCaller.class.getPackage())
      .build();

  /**
   * A stack trace sanitizer that does not do anything.
   */
  private static final StackTraceSanitizer EMPTY = new StackTraceSanitizer(emptyList());

  /**
   * A list of all predicates that remove matching elements
   */
  private final @Unmodifiable List<Predicate<StackTraceElement>> filters;

  /**
   * Strips all the stack trace elements that meet the criteria of any filter.
   *
   * @param throwable Throwable to strip
   */
  public void sanitize(@NotNull Throwable throwable) {
    if (filters.isEmpty()) {
      return;
    }
    if (throwable.getCause() != null) {
      sanitize(throwable.getCause());
    }
    List<StackTraceElement> trace = listOf(throwable.getStackTrace());
    int stripIndex = trace.size();
    for (int i = 0; i < trace.size(); i++) {
      StackTraceElement stackTraceElement = trace.get(i);
      if (filters.stream().anyMatch(f -> f.test(stackTraceElement))) {
        stripIndex = i;
        break;
      }
    }
    trace.subList(stripIndex, trace.size()).clear();
    throwable.setStackTrace(trace.toArray(new StackTraceElement[0]));
  }

  private StackTraceSanitizer(@Unmodifiable List<Predicate<StackTraceElement>> filters) {
    this.filters = filters;
  }

  /**
   * Returns the default sanitizer
   *
   * @return The default stack trace sanitizer
   */
  public static StackTraceSanitizer defaultSanitizer() {
    return DEFAULT_SANITIZER;
  }

  /**
   * Returns the empty sanitizer
   *
   * @return A sanitizer that does not modify the trace
   */
  public static StackTraceSanitizer empty() {
    return EMPTY;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final List<Predicate<StackTraceElement>> filters = new ArrayList<>();

    /**
     * Strips all the given classes from the trace
     *
     * @param classes Classes to strip
     * @return This builder
     */
    public Builder ignoreClasses(@NotNull Class<?>... classes) {
      for (Class<?> clazz : classes) {
        filters.add(c -> c.getClassName().equals(clazz.getName()));
      }
      return this;
    }

    /**
     * Strips all classes that belong to the given package name
     *
     * @param packageName Package name to strip
     * @return This builder
     */
    public Builder ignorePackage(@NotNull String packageName) {
      filters.add(c -> c.getClassName().startsWith(packageName));
      return this;
    }

    /**
     * Strips all classes that belong to the given package
     *
     * @param pkg Package to strip its classes
     * @return This builder
     */

    public Builder ignorePackage(@NotNull Package pkg) {
      filters.add(c -> c.getClassName().startsWith(pkg.getName()));
      return this;
    }

    /**
     * Strips all traces that point to the given method name
     *
     * @param methodName The method name to strip
     * @return This builder
     */
    public Builder ignoreMethod(@NotNull String methodName) {
      filters.add(c -> c.getMethodName().equals(methodName));
      return this;
    }

    /**
     * Strips all native methods.
     *
     * @return This builder
     */
    public Builder ignoreNativeMethods() {
      filters.add(StackTraceElement::isNativeMethod);
      return this;
    }

    /**
     * Constructs the {@link StackTraceSanitizer} from the configuration
     *
     * @return The newly created sanitizer
     */
    public StackTraceSanitizer build() {
      return new StackTraceSanitizer(Collections.unmodifiableList(new ArrayList<>(filters)));
    }
  }

}
