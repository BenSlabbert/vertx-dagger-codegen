/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.annotation.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface SecuredProxy {

  @interface RoleBasedPermission {

    String role();

    String resource() default "";
  }

  @interface PermissionBasedPermission {

    String permission();

    String resource() default "";
  }

  @interface WildcardBasedPermission {

    String permission();

    String resource() default "";
  }

  @interface SecuredAction {

    String group();

    String role();

    String[] permissions() default {};
  }
}
