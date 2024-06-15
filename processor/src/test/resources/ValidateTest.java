/* Licensed under Apache-2.0 2024. */
package my.test;

import github.benslabbert.vertxdaggercodegen.annotation.validation.GenerateJakartaValidator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@GenerateJakartaValidator
public record ValidateTest(
    @NotBlank String name,
    @NotNull @Min(18) @Max(99) Integer age,
    @NotBlank @Size(min = 1, max = 10) String address,
    @NotEmpty @Size(min = 1, max = 10) List<String> hobbies,
    @NotEmpty List<@NotBlank String> friends) {

  public static boolean isValid(ValidateTest in) {
    return ValidateTest_ParamValidator.validate(in);
  }
}
