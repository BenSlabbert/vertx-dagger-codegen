/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.advice;

import github.benslabbert.vertxdaggercodegen.annotation.advice.Advice;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MeasureAdvice implements Advice {

  @Inject
  MeasureAdvice() {}

  @Override
  public void before(Class<?> clazz, String methodName, Object... args) {
    System.err.println("before " + clazz.getName() + "." + methodName);
  }

  @Override
  public void after(Class<?> clazz, String methodName, Object result) {
    System.err.println("after " + clazz.getName() + "." + methodName);
  }
}
