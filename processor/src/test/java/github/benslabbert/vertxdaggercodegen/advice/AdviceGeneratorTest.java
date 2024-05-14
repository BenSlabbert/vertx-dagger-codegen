/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.advice;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import java.net.URL;
import org.junit.jupiter.api.Test;

class AdviceGeneratorTest {

  @Test
  void test() {
    URL resource = this.getClass().getClassLoader().getResource("AdviceTest.java");
    assertThat(resource).isNotNull();

    assertAbout(JavaSourceSubjectFactory.javaSource())
        .that(JavaFileObjects.forResource(resource))
        .withCompilerOptions(
            "-AprocessCustom=github.benslabbert.vertxdaggercodegen.example.custom.Custom")
        .processedWith(new AdviceGenerator())
        .compilesWithoutError();
  }
}
