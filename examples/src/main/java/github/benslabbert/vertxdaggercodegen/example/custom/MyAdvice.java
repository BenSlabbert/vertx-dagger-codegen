/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.custom;

import github.benslabbert.vertxdaggercodegen.annotation.advice.Advice;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MyAdvice implements Advice {

  @Inject
  MyAdvice() {}

  public void customize() {}

  @Override
  public void before(Class<?> clazz, String methodName, Object... args) {}

  @Override
  public void after(Class<?> clazz, String methodName, Object result) {}
}
