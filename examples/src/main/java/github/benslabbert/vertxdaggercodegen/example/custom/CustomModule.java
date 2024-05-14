/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.custom;

import dagger.Module;

@Module(includes = ModuleBindings.class)
public interface CustomModule {

  Client client();
}
