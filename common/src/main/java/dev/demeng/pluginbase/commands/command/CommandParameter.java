package dev.demeng.pluginbase.commands.command;

import dev.demeng.pluginbase.commands.CommandHandler;
import dev.demeng.pluginbase.commands.annotation.Default;
import dev.demeng.pluginbase.commands.annotation.Description;
import dev.demeng.pluginbase.commands.annotation.Flag;
import dev.demeng.pluginbase.commands.annotation.Named;
import dev.demeng.pluginbase.commands.annotation.Optional;
import dev.demeng.pluginbase.commands.annotation.Single;
import dev.demeng.pluginbase.commands.annotation.Switch;
import dev.demeng.pluginbase.commands.autocomplete.SuggestionProvider;
import dev.demeng.pluginbase.commands.command.trait.CommandAnnotationHolder;
import dev.demeng.pluginbase.commands.command.trait.PermissionHolder;
import dev.demeng.pluginbase.commands.process.ParameterResolver;
import dev.demeng.pluginbase.commands.process.ParameterValidator;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Represents a parameter in a command method. This corresponds to a {@link Parameter} in a method.
 * <p>
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public interface CommandParameter extends Comparable<CommandParameter>, PermissionHolder,
    CommandAnnotationHolder {

  /**
   * Returns the parameter name, either from the {@code @Named} annotation or the compiler-reserved
   * name 'argN'.
   *
   * @return The parameter name
   * @see Named
   */
  @NotNull String getName();

  /**
   * Returns the description of this parameter. This can be specified with {@link Description}.
   * <p>
   * This may be leveraged by some platforms for completions or suggestions.
   *
   * @return The parameter description.
   * @see Description
   */
  @Nullable String getDescription();

  /**
   * Returns the index of this parameter in the method.
   *
   * @return The index of this parameter
   */
  int getMethodIndex();

  /**
   * Returns the actual index of the parameter in the command.
   * <p>
   * If this parameter does not {@link ParameterResolver#mutatesArguments()}, this will return -1.
   *
   * @return The command index
   */
  int getCommandIndex();

  /**
   * The runtime-present type of this parameter
   *
   * @return The parameter type
   */
  @NotNull Class<?> getType();

  /**
   * Returns the <i>full</i> type of this parameter. For example, if this is a
   * <code>List&lt;String&gt;</code> then this will return that full type.
   *
   * @return The full type, including generics.
   */
  @NotNull Type getFullType();

  /**
   * The default value. This is set when annotated by {@link Default}.
   *
   * @return The parameter's default value
   */
  @NotNull @Unmodifiable List<String> getDefaultValue();

  /**
   * Whether should this parameter consume all arguments that come after it.
   * <p>
   * This will return {@code true} if, and only if this is the last parameter in the method and is
   * annotated with {@link Single}.
   *
   * @return Whether should this parameter consume all arguments
   */
  boolean consumesAllString();

  /**
   * Returns the Java-counterpart parameter of this
   *
   * @return The reflection parameter
   */
  Parameter getJavaParameter();

  /**
   * Returns the {@link SuggestionProvider} of this parameter. If not specified, this will return
   * {@link SuggestionProvider#EMPTY}.
   *
   * @return The parameter's suggestion provider.
   */
  @NotNull SuggestionProvider getSuggestionProvider();

  /**
   * Returns all the validators of the parameter
   *
   * @return The parameter validators
   */
  @NotNull @Unmodifiable List<ParameterValidator<Object>> getValidators();

  /**
   * Whether is this parameter optional or not. This is whether the parameter has the
   * {@link Optional} annotation or not
   *
   * @return Whether is this parameter optional
   */
  boolean isOptional();

  /**
   * Returns whether this parameter is the last in the method
   *
   * @return If this parameter is the last in method
   */
  boolean isLastInMethod();

  /**
   * Returns whether is this parameter a {@link Switch} parameter or not.
   *
   * @return Whether is this parameter a switch or not
   */
  boolean isSwitch();

  /**
   * Returns the name of the switch. Returns null if {@link #isSwitch()} is false.
   *
   * @return The switch name, otherwise throws a {@link IllegalStateException}
   * @throws IllegalStateException If this parameter is not a switch
   */
  @NotNull String getSwitchName();

  /**
   * Returns whether is this parameter a {@link Flag} parameter or not
   *
   * @return Whether is this parameter a flag or not
   */
  boolean isFlag();

  /**
   * Returns the name of the flag. Returns null if {@link #isFlag()} is false.
   *
   * @return The flag name, otherwise throws a {@link IllegalStateException}
   * @throws IllegalStateException If this parameter is not a switch
   */
  @NotNull String getFlagName();

  /**
   * Returns the default {@link Switch#defaultValue()} if the switch was not provided in the
   * command.
   * <p>
   * Note that this will return {@code false} if {@link #isSwitch()} is {@code false}.
   *
   * @return The default switch value.
   */
  boolean getDefaultSwitch();

  /**
   * Returns the resolver for this parameter. See {@link ParameterResolver} for more information.
   *
   * @return The resolver
   * @see ParameterResolver
   */
  @NotNull <T> ParameterResolver<T> getResolver();

  /**
   * Returns the command handler that instantiated this parameter
   *
   * @return The owning command handler
   */
  @NotNull CommandHandler getCommandHandler();

  /**
   * Returns the command that declares this parameter
   *
   * @return The declaring command for this parameter
   * @since 1.3.0
   */
  @NotNull ExecutableCommand getDeclaringCommand();

  /**
   * Returns the required permission to access this parameter.
   * <p>
   * Parameters by default inherit their parent {@link #getDeclaringCommand()} permission, unless
   *
   * @return The command permission
   */
  @NotNull CommandPermission getPermission();
}
