/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.example.security;

import github.benslabbert.vertxdaggercodegen.annotation.security.SecuredProxy;

@SecuredProxy
public interface SecuredRpc {

  @SecuredProxy.SecuredAction(
      group = "group",
      role = "role",
      permissions = {"p1", "p2"})
  void action1();

  @SecuredProxy.SecuredAction(
      group = "group",
      role = "role",
      permissions = {"p3"})
  void action2();

  @SecuredProxy.SecuredAction(group = "group", role = "role")
  void action3();
}
