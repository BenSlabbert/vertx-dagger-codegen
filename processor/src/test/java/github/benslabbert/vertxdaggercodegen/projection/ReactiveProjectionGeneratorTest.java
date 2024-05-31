/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.projection;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import java.net.URL;
import org.junit.jupiter.api.Test;

class ReactiveProjectionGeneratorTest {

  @Test
  void test() {
    URL resource = this.getClass().getClassLoader().getResource("ReactiveProjectionTest.java");
    assertThat(resource).isNotNull();

    assertAbout(JavaSourceSubjectFactory.javaSource())
        .that(JavaFileObjects.forResource(resource))
        .processedWith(new ReactiveProjectionGenerator())
        .compilesWithoutError();
  }
}
