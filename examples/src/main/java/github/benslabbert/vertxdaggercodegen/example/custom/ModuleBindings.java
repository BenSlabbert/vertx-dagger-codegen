/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.custom;

import dagger.Binds;
import dagger.Module;

@Module
interface ModuleBindings {

  @Binds
  MyClass myClass(MyClass_Advised myClass);

  @Binds
  OnlyAdvised onlyAdvised(OnlyAdvised_Advised myClass);

  @Binds
  OnlyCustom onlyCustom(OnlyCustom_Advised myClass);
}
