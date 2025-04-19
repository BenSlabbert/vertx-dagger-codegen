/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.example.serviceproxy.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxjsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record ResponseDto(String resp) {

  public static Builder builder() {
    return new AutoBuilder_ResponseDto_Builder();
  }

  public static ResponseDto fromJson(JsonObject json) {
    return ResponseDto_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return ResponseDto_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder resp(String resp);

    ResponseDto build();
  }
}
