/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.client;

import dagger.Module;
import github.benslabbert.vertxdaggercodegen.example.advice.AdviceModule;

@Module(includes = {AdviceModule.class, ModuleBindings.class})
public interface ClientModule {

  Example example();
}
