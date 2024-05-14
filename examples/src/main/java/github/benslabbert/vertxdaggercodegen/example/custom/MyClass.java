/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.custom;

import github.benslabbert.vertxdaggercodegen.annotation.advice.Advised;
import github.benslabbert.vertxdaggercodegen.example.client.LogAdvice;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Advised(advisors = {LogAdvice.class})
class MyClass {

  @Inject
  MyClass() {}

  @Custom(param1 = "foo", bar = true)
  public void method() {}
}
