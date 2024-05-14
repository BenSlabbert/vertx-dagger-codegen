/* Licensed under Apache-2.0 2023. */
package github.benslabbert.vertxdaggercodegen.commons;

public class GenerationException extends RuntimeException {

  public GenerationException(Throwable cause) {
    super(cause);
  }

  public GenerationException(String message) {
    super(message);
  }
}
