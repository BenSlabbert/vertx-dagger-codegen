/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.commons.security.rpc;

import com.google.auto.value.AutoOneOf;

@AutoOneOf(SecuredUnion.Kind.class)
public abstract class SecuredUnion {

  public static SecuredUnion securedAction(SecuredAction securedAction) {
    return AutoOneOf_SecuredUnion.securedAction(securedAction);
  }

  public static SecuredUnion role(Role role) {
    return AutoOneOf_SecuredUnion.role(role);
  }

  public static SecuredUnion permission(Permission permission) {
    return AutoOneOf_SecuredUnion.permission(permission);
  }

  public static SecuredUnion wildcard(Wildcard wildcard) {
    return AutoOneOf_SecuredUnion.wildcard(wildcard);
  }

  public enum Kind {
    SECURED_ACTION,
    ROLE,
    Permission,
    WILDCARD
  }

  public abstract Kind getKind();

  public abstract SecuredAction securedAction();

  public abstract Role role();

  public abstract Permission permission();

  public abstract Wildcard wildcard();
}
