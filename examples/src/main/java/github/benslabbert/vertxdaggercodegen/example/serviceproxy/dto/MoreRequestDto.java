/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.example.serviceproxy.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record MoreRequestDto() {

  public static Builder builder() {
    return new AutoBuilder_MoreRequestDto_Builder();
  }

  public static MoreRequestDto fromJson(JsonObject ignore) {
    return new MoreRequestDto();
  }

  public JsonObject toJson() {
    return new JsonObject();
  }

  @AutoBuilder
  public interface Builder {

    MoreRequestDto build();
  }
}
