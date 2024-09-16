/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.example.security;

import github.benslabbert.vertxdaggercodegen.annotation.security.SecuredProxy;
import github.benslabbert.vertxdaggercodegen.annotation.security.SecuredProxy.PermissionBasedPermission;
import github.benslabbert.vertxdaggercodegen.annotation.security.SecuredProxy.RoleBasedPermission;
import github.benslabbert.vertxdaggercodegen.annotation.security.SecuredProxy.SecuredAction;
import github.benslabbert.vertxdaggercodegen.annotation.security.SecuredProxy.WildcardBasedPermission;

@SecuredProxy
public interface SecuredRpc {

  @SecuredAction(
      group = "group",
      role = "role",
      permissions = {"p1", "p2"})
  void action1();

  @SecuredAction(
      group = "group",
      role = "role",
      permissions = {"p3"})
  void action2();

  @SecuredAction(group = "group", role = "role")
  void action3();

  @RoleBasedPermission(role = "role", resource = "resource")
  void role();

  @PermissionBasedPermission(permission = "permission", resource = "resource")
  void permission();

  @WildcardBasedPermission(permission = "wildcard", resource = "resource")
  void wildcard();
}
