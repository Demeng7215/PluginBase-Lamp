package dev.demeng.pluginbase.commands.bukkit.annotation;

import dev.demeng.pluginbase.commands.annotation.DistributeOnMethods;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bukkit.permissions.PermissionDefault;

/**
 * Adds a command permission for the given command
 */
@DistributeOnMethods
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandPermission {

  /**
   * The permission node
   *
   * @return The permission node
   */
  String value();

  /**
   * Who can use this command by default.
   *
   * @return Permission's default access. Default is {@link PermissionDefault#OP}.
   */
  PermissionDefault defaultAccess() default PermissionDefault.OP;

}
