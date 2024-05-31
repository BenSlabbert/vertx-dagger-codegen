/* Licensed under Apache-2.0 2024. */
package my.test;

import github.benslabbert.vertxdaggercodegen.annotation.projection.Column;
import github.benslabbert.vertxdaggercodegen.annotation.projection.ReactiveProjection;

@ReactiveProjection
public record ReactiveProjectionTest(@Column(name = "name") String name, String surname) {

  public static Builder builder() {
    return new Builder() {
      private String name;
      private String surname;

      @Override
      public Builder name(String name) {
        this.name = name;
        return this;
      }

      @Override
      public Builder surname(String surname) {
        this.surname = surname;
        return this;
      }

      @Override
      public ReactiveProjectionTest build() {
        return new ReactiveProjectionTest(name, surname);
      }
    };
  }

  public interface Builder {

    Builder name(String name);

    Builder surname(String surname);

    ReactiveProjectionTest build();
  }
}
