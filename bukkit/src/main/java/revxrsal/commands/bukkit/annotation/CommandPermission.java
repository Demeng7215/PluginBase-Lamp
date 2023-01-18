package revxrsal.commands.bukkit.annotation;

import org.bukkit.permissions.PermissionDefault;
import revxrsal.commands.annotation.DistributeOnMethods;
import revxrsal.commands.annotation.NotSender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a command permission for the given command
 */
@DistributeOnMethods
@NotSender.ImpliesNotSender
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
