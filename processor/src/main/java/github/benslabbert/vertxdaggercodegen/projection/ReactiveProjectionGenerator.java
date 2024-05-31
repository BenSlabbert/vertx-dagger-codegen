/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.projection;

import com.google.auto.service.AutoService;
import github.benslabbert.vertxdaggercodegen.annotation.projection.Column;
import github.benslabbert.vertxdaggercodegen.annotation.projection.ReactiveProjection;
import github.benslabbert.vertxdaggercodegen.commons.GenerationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class ReactiveProjectionGenerator extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(ReactiveProjection.class.getCanonicalName());
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

  private void process(Element elementToBeAdvised) throws IOException {
    List<MappedField> mappedFields =
        elementToBeAdvised.getEnclosedElements().stream()
            .filter(e -> e.getKind().isField())
            .map(this::processField)
            .toList();

    String canonicalName = elementToBeAdvised.asType().toString();
    String classPackage = canonicalName.substring(0, canonicalName.lastIndexOf('.'));
    Name projectionName = elementToBeAdvised.getSimpleName();
    String generatedClassName = projectionName + "_ReactiveRowMapper";

    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.printf("package %s;%n", classPackage);
      out.println();

      out.println("import io.vertx.sqlclient.templates.RowMapper;");
      out.println("import io.vertx.sqlclient.Row;");
      out.println("import java.util.stream.Collector;");
      out.println("import java.util.stream.Collectors;");
      out.println("import java.util.List;");
      out.println("import javax.annotation.processing.Generated;");
      out.println();

      out.printf(
          "@Generated(value = \"%s\", date = \"%s\")%n",
          getClass().getCanonicalName(),
          LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
      out.printf(
          "public interface %s extends RowMapper<%s> {%n", generatedClassName, projectionName);

      out.println();
      out.printf("\t%s INSTANCE = new %s() { };%n", generatedClassName, generatedClassName);
      out.println();
      out.println(
          "\tCollector<Row, ?, List<"
              + projectionName
              + ">> COLLECTOR = Collectors.mapping(INSTANCE::map, Collectors.toList());");
      out.println();
      out.println("\tdefault " + projectionName + " map(Row row) {");

      out.printf("\t\t%s.Builder builder = %s.builder();%n", projectionName, projectionName);
      out.println("\t\tint idx;");

      for (MappedField mappedField : mappedFields) {
        out.printf(
            "\t\tif ((idx = row.getColumnIndex(\"%s\")) != -1) {%n", mappedField.columnName());

        out.printf("\t\t\tvar val = row.%s(idx);%n", getGetter(mappedField));
        out.println("\t\t\tif (null != val) {");
        out.println("\t\t\t\tbuilder." + mappedField.name() + "(val);");
        out.println("\t\t\t}");
        out.println("\t\t}");
      }

      out.println("\t\treturn builder.build();");
      out.println("\t}");

      out.println("}");
      out.println();
    }
  }

  private static String getGetter(MappedField mappedField) {
    return switch (mappedField.typeCanonicalName()) {
      case "java.lang.String" -> "getString";
      case "java.lang.Integer", "int" -> "getInteger";
      case "java.lang.Long", "long" -> "getLong";
      case "java.lang.Boolean", "boolean" -> "getBoolean";
      case "java.lang.Short", "short" -> "getShort";
      case "java.lang.Float", "float" -> "getFloat";
      case "java.lang.Double", "double" -> "getDouble";
      case "java.util.UUID" -> "getUUID";
      case "java.time.LocalTime" -> "getLocalTime";
      case "java.time.LocalDateTime" -> "getLocalDateTime";
      case "java.time.OffsetTime" -> "getOffsetTime";
      case "java.time.OffsetDateTime" -> "getOffsetDateTime";
      default ->
          throw new IllegalStateException("Unexpected value: " + mappedField.typeCanonicalName());
    };
  }

  private MappedField processField(Element field) {
    String fieldName = field.getSimpleName().toString();
    Column column = field.getAnnotation(Column.class);
    String columnName = fieldName;
    if (null != column && !column.name().isEmpty()) {
      columnName = column.name();
    }

    String typeCanonicalName = field.asType().toString();
    return new MappedField(fieldName, columnName, typeCanonicalName);
  }

  private record MappedField(String name, String columnName, String typeCanonicalName) {}
}
