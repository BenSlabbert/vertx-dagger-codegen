/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.example;

import github.benslabbert.vertxdaggercodegen.example.validation.RequestDto;
import java.util.List;

public class Main {

  public static void main(String[] args) {
    RequestDto requestDto = new RequestDto("", 1, "", List.of(), List.of("friend", ""));
    System.err.println("RequestDto is valid: " + requestDto.isValid());
  }
}
