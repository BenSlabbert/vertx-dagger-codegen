/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.example.serviceproxy.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record MoreResponseDto() {

  public static Builder builder() {
    return new AutoBuilder_MoreResponseDto_Builder();
  }

  public static MoreResponseDto fromJson(JsonObject ignore) {
    return new MoreResponseDto();
  }

  public JsonObject toJson() {
    return new JsonObject();
  }

  @AutoBuilder
  public interface Builder {
    MoreResponseDto build();
  }
}
