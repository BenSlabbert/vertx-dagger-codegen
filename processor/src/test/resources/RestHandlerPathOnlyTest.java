/* Licensed under Apache-2.0 2024. */
package my.test;

import github.benslabbert.vertxdaggercodegen.annotation.url.RestHandler;

public class RestHandlerPathOnlyTest {

  private static final String PATH =
      "/some/prefix/{int:param1=1}/path/{string:param2=abc}/more-path";

  @RestHandler(path = PATH)
  public void handler() {}
}
