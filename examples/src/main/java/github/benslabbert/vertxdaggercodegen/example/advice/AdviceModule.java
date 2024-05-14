/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.advice;

import dagger.Module;

@Module
public interface AdviceModule {

  DependencyB dependencyB();

  MeasureAdvice measureAdvice();
}
