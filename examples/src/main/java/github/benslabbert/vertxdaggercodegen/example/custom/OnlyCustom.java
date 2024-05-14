/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.custom;

import github.benslabbert.vertxdaggercodegen.annotation.advice.Advised;
import java.util.Map;
import javax.inject.Inject;

@Advised
class OnlyCustom {

  @Inject
  OnlyCustom() {}

  @MyAdvisor
  public Map<String, Integer> method() {
    return Map.of();
  }
}
