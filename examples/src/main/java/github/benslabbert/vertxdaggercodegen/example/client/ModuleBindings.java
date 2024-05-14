/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.client;

import dagger.Binds;
import dagger.Module;

@Module
interface ModuleBindings {

  @Binds
  Example example(Example_Advised advised);
}
