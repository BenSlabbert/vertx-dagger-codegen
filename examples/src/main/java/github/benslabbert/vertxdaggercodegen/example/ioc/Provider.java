/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.example.ioc;

import dagger.BindsInstance;
import dagger.Component;
import github.benslabbert.vertxdaggercodegen.example.advice.AdviceModule;
import github.benslabbert.vertxdaggercodegen.example.advice.DependencyB;
import github.benslabbert.vertxdaggercodegen.example.advice.MeasureAdvice;
import github.benslabbert.vertxdaggercodegen.example.client.ClientModule;
import github.benslabbert.vertxdaggercodegen.example.client.Example;
import github.benslabbert.vertxdaggercodegen.example.custom.Client;
import github.benslabbert.vertxdaggercodegen.example.custom.CustomModule;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AdviceModule.class, ClientModule.class, CustomModule.class})
public interface Provider {

  Example example();

  DependencyB dependencyB();

  MeasureAdvice measureAdvice();

  Client client();

  String string();

  int integer();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder string(String str);

    @BindsInstance
    Builder integer(int i);

    Provider build();
  }
}
