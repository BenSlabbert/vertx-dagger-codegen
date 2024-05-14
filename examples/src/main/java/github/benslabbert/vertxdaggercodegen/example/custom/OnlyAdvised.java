/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.custom;

import github.benslabbert.vertxdaggercodegen.annotation.advice.Advised;
import github.benslabbert.vertxdaggercodegen.example.client.LogAdvice;
import javax.inject.Inject;

@Advised(advisors = {LogAdvice.class})
class OnlyAdvised {

  @Inject
  OnlyAdvised() {}

  public void method() {}
}
