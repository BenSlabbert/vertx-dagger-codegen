/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.example.serviceproxy.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record RequestDto(String req) {

  public static Builder builder() {
    return new AutoBuilder_RequestDto_Builder();
  }

  public static RequestDto fromJson(JsonObject json) {
    return RequestDto_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return RequestDto_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder req(String req);

    RequestDto build();
  }
}
