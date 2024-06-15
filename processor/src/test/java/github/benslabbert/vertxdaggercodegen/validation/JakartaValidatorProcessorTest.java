/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.validation;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import java.net.URL;
import org.junit.jupiter.api.Test;

class JakartaValidatorProcessorTest {

  @Test
  void test() {
    URL resource = this.getClass().getClassLoader().getResource("ValidateTest.java");
    assertThat(resource).isNotNull();

    assertAbout(JavaSourceSubjectFactory.javaSource())
        .that(JavaFileObjects.forResource(resource))
        .processedWith(new JakartaValidatorProcessor())
        .compilesWithoutError();
  }
}
