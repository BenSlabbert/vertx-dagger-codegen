/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.validation;

import com.google.auto.service.AutoService;
import github.benslabbert.vertxdaggercodegen.annotation.validation.GenerateJakartaValidator;
import github.benslabbert.vertxdaggercodegen.commons.GenerationException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class JakartaValidatorProcessor extends AbstractProcessor {

  private static final String VALIDATOR_METHODS =
      """
                    private static <T> boolean forEach(Iterable<T> i, Predicate<T> predicate) {
                        for (T t : i) {
                            boolean test = predicate.test(t);
                            if (!test) {
                                return false;
                            }
                        }

                        return true;
                    }

                    private static boolean notEmpty(CharSequence c) {
                      return null != c && !c.isEmpty();
                    }

                    private static boolean notEmpty(Collection<?> c) {
                      return null != c && !c.isEmpty();
                    }

                    private static boolean notEmpty(Map<?, ?> c) {
                      return null != c && !c.isEmpty();
                    }

                    private static <T> boolean notEmpty(T[] c) {
                      return null != c && c.length > 0;
                    }

                    private static boolean size(CharSequence cs, int min, int max) {
                      if (null == cs) {
                        return true;
                      }

                      return cs.length() >= min && cs.length() <= max;
                    }

                    private static boolean size(Collection<?> c, int min, int max) {
                      if (null == c) {
                        return true;
                      }

                      return c.size() >= min && c.size() <= max;
                    }

                    private static boolean size(Map<?, ?> c, int min, int max) {
                      if (null == c) {
                        return true;
                      }

                      return c.size() >= min && c.size() <= max;
                    }

                    private static <T> boolean size(T[] a, int min, int max) {
                      if (null == a) {
                        return true;
                      }

                      return a.length >= min && a.length <= max;
                    }

                    private static boolean notBlank(CharSequence o) {
                      return null != o && !o.isEmpty() && o.chars().filter(c -> c == ' ').findFirst().isEmpty();
                    }

                    private static boolean min(Integer n, long min) {
                      return n >= min;
                    }

                    private static boolean min(Long n, long min) {
                      return n >= min;
                    }

                    private static boolean min(Short n, long min) {
                      return n >= min;
                    }

                    private static boolean min(Byte n, long min) {
                      return n >= min;
                    }

                    private static boolean min(Float n, long min) {
                      return n.longValue() >= min;
                    }

                    private static boolean min(Double n, long min) {
                      return n.longValue() >= min;
                    }

                    private static boolean min(BigDecimal n, long min) {
                      return n.compareTo(BigDecimal.valueOf(min)) >= 0;
                    }

                    private static boolean min(BigInteger n, long min) {
                      return n.compareTo(BigInteger.valueOf(min)) >= 0;
                    }

                    private static boolean max(Integer n, long max) {
                      return n <= max;
                    }

                    private static boolean max(Long n, long max) {
                      return n <= max;
                    }

                    private static boolean max(Short n, long max) {
                      return n <= max;
                    }

                    private static boolean max(Byte n, long max) {
                      return n <= max;
                    }

                    private static boolean max(Float n, long max) {
                      return n.longValue() <= max;
                    }

                    private static boolean max(Double n, long max) {
                      return n.longValue() <= max;
                    }

                    private static boolean max(BigDecimal n, long max) {
                      return n.compareTo(BigDecimal.valueOf(max)) <= 0;
                    }

                    private static boolean max(BigInteger n, long max) {
                      return n.compareTo(BigInteger.valueOf(max)) <= 0;
                    }

                    private static boolean isMaxSize(List<?> list, int max) {
                      return null != list && list.size() <= max;
                    }

                    private static boolean notNull(Object o) {
                      return null != o;
                    }
                    """;

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(GenerateJakartaValidator.class.getCanonicalName());
  }

  @Override
  public Set<String> getSupportedOptions() {
    return Set.of();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (annotations.isEmpty()) {
      return false;
    }

    for (TypeElement annotation : annotations) {
      Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(annotation);
      for (Element element : annotated) {
        try {
          process(element);
        } catch (Exception e) {
          throw new GenerationException(e);
        }
      }
    }

    return true;
  }

  private record FieldValidations(
      boolean notEmpty,
      boolean notBlank,
      boolean notNull,
      Min min,
      Max max,
      Size size,
      ForEach forEach) {

    record ForEach(boolean notBlank) {}

    record Min(long min) {}

    record Max(long max) {}

    record Size(int min, int max) {}

    private boolean empty() {
      return !notEmpty
          && !notBlank
          && !notNull
          && min == null
          && max == null
          && size == null
          && forEach == null;
    }
  }

  private FieldValidations getFieldValidations(Element e) {
    boolean notEmpty = false;
    boolean notBlank = false;
    boolean notNull = false;
    FieldValidations.Min min = null;
    FieldValidations.Max max = null;
    FieldValidations.Size size = null;
    FieldValidations.ForEach forEach = null;

    for (AnnotationMirror annotationMirror : e.getAnnotationMirrors()) {
      String annotationClassName = annotationMirror.getAnnotationType().toString();
      if (annotationClassName.equals(NotBlank.class.getCanonicalName())) {
        notBlank = true;
      }
      if (annotationClassName.equals(NotNull.class.getCanonicalName())) {
        notNull = true;
      }
      if (annotationClassName.equals(NotEmpty.class.getCanonicalName())) {
        notEmpty = true;
      }
      if (annotationClassName.equals(Size.class.getCanonicalName())) {
        Size annotation = e.getAnnotation(Size.class);
        size = new FieldValidations.Size(annotation.min(), annotation.max());
      }
      if (annotationClassName.equals(Min.class.getCanonicalName())) {
        Min annotation = e.getAnnotation(Min.class);
        min = new FieldValidations.Min(annotation.value());
      }
      if (annotationClassName.equals(Max.class.getCanonicalName())) {
        Max annotation = e.getAnnotation(Max.class);
        max = new FieldValidations.Max(annotation.value());
      }

      if (TypeKind.DECLARED == e.asType().getKind()) {
        DeclaredType dt = (DeclaredType) e.asType();
        for (TypeMirror typeArgument : dt.getTypeArguments()) {
          Optional<? extends AnnotationMirror> first =
              typeArgument.getAnnotationMirrors().stream()
                  .filter(
                      f -> {
                        String string = f.getAnnotationType().toString();
                        String canonicalName = NotBlank.class.getCanonicalName();
                        return string.equals(canonicalName);
                      })
                  .findFirst();

          if (first.isPresent()) {
            forEach = new FieldValidations.ForEach(true);
          }
        }
      }
    }

    return new FieldValidations(notEmpty, notBlank, notNull, min, max, size, forEach);
  }

  private void process(Element classToBeAdvised) throws IOException {
    String canonicalName = classToBeAdvised.asType().toString();
    String classPackage = canonicalName.substring(0, canonicalName.lastIndexOf('.'));
    Name classUnderTestName = classToBeAdvised.getSimpleName();
    String generatedClassName = classUnderTestName + "_" + "ParamValidator";

    Map<String, FieldValidations> fieldWithValidations =
        classToBeAdvised.getEnclosedElements().stream()
            .filter(e -> e.getKind().isField())
            .collect(
                Collectors.toMap(e -> e.getSimpleName().toString(), this::getFieldValidations));

    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.printf("package %s;%n", classPackage);
      out.println();
      out.println("import java.math.BigDecimal;");
      out.println("import java.math.BigInteger;");
      out.println("import java.util.Collection;");
      out.println("import java.util.Iterator;");
      out.println("import java.util.List;");
      out.println("import java.util.Map;");
      out.println("import java.util.function.Predicate;");
      out.println("import javax.annotation.processing.Generated;");
      out.println();

      out.printf(
          "@Generated(value = \"%s\", date = \"%s\")%n",
          getClass().getCanonicalName(),
          LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
      out.printf("final class %s {%n", generatedClassName);
      out.println();
      out.println("\tprivate " + generatedClassName + "() {}");
      out.println();

      out.printf("\tpublic static boolean validate(%s in) {%n", classUnderTestName);

      for (Map.Entry<String, FieldValidations> entry : fieldWithValidations.entrySet()) {
        String fieldName = entry.getKey();
        FieldValidations validations = entry.getValue();

        if (validations.empty()) {
          continue;
        }

        List<String> strings = new ArrayList<>();

        if (validations.notBlank()) {
          strings.add("notBlank(in.%s())".formatted(fieldName));
        }

        if (validations.notEmpty()) {
          strings.add("notEmpty(in.%s())".formatted(fieldName));
        }

        if (validations.notNull()) {
          strings.add("notNull(in.%s())".formatted(fieldName));
        }

        if (null != validations.min()) {
          strings.add("min(in.%s(), %d)".formatted(fieldName, validations.min().min()));
        }

        if (null != validations.max()) {
          strings.add("max(in.%s(), %d)".formatted(fieldName, validations.max().max()));
        }

        if (null != validations.size()) {
          strings.add(
              "size(in.%s(), %d, %d)"
                  .formatted(fieldName, validations.size().min(), validations.size().max()));
        }

        if (null != validations.forEach()) {
          if (validations.forEach().notBlank()) {
            strings.add("forEach(in.%s(), %s::notBlank)".formatted(fieldName, generatedClassName));
          }
        }

        String joined = String.join(" && ", strings);
        out.printf("\t\tboolean %sValid = %s;%n", fieldName, joined);
      }

      String combinedCondition =
          fieldWithValidations.keySet().stream()
              .map(s -> s + "Valid")
              .collect(Collectors.joining(" && "));

      out.printf("\t\treturn %s;%n", combinedCondition);
      out.println("\t}");
      out.println();

      out.println("\t" + VALIDATOR_METHODS.replace("\n", "\n\t"));

      out.println("}");
    }
  }
}
