/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.example.projection;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxdaggercodegen.annotation.projection.Column;
import github.benslabbert.vertxdaggercodegen.annotation.projection.ReactiveProjection;
import java.util.UUID;

@ReactiveProjection
public record Reactive(
    @Column(name = "name") String name,
    String surname,
    @Column(name = "id_col") long id,
    int inti,
    Integer integer,
    boolean active,
    UUID uuid) {

  public static Builder builder() {
    return new AutoBuilder_Reactive_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder name(String name);

    Builder surname(String surname);

    Builder id(long id);

    Builder inti(int inti);

    Builder integer(Integer integer);

    Builder active(boolean active);

    Builder uuid(UUID uuid);

    Reactive build();
  }
}
