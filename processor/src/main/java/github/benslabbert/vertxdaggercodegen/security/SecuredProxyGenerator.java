/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.security;

import github.benslabbert.vertxdaggercodegen.annotation.security.SecuredProxy;
import github.benslabbert.vertxdaggercodegen.commons.GenerationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

public class SecuredProxyGenerator extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(SecuredProxy.class.getCanonicalName());
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
    if (ElementKind.INTERFACE != elementToBeAdvised.getKind()) {
      throw new GenerationException(
          "Only interfaces can be annotated with: " + getSupportedAnnotationTypes());
    }

    List<SecuredMethod> securedMethods = getSecuredMethods(elementToBeAdvised);

    generateFile(elementToBeAdvised, securedMethods);
  }

  private void generateFile(Element elementToBeAdvised, List<SecuredMethod> securedMethods)
      throws IOException {

    String canonicalName = elementToBeAdvised.asType().toString();
    String classPackage = canonicalName.substring(0, canonicalName.lastIndexOf('.'));
    Name superClass = elementToBeAdvised.getSimpleName();

    String generatedClassName = superClass + "_SecuredActions";

    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.printf("package %s;%n", classPackage);
      out.println();
      out.println(
          "import github.benslabbert.vertxdaggercodegen.commons.security.rpc.SecuredAction;");
      out.println(
          "import github.benslabbert.vertxdaggercodegen.commons.security.rpc.SecuredUnion;");
      out.println("import github.benslabbert.vertxdaggercodegen.commons.security.rpc.Role;");
      out.println("import github.benslabbert.vertxdaggercodegen.commons.security.rpc.Permission;");
      out.println("import github.benslabbert.vertxdaggercodegen.commons.security.rpc.Wildcard;");
      out.println();
      out.println("import java.util.List;");
      out.println("import java.util.Map;");
      out.println("import javax.annotation.processing.Generated;");
      out.println();

      out.printf(
          "@Generated(value = \"%s\", date = \"%s\")%n",
          getClass().getCanonicalName(),
          LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
      out.printf("public final class %s {%n", generatedClassName);
      out.println();

      out.println("\tpublic static Map<String, SecuredUnion> getSecuredActions() {");
      out.println("\t\treturn Map.ofEntries(");
      boolean isLast = false;
      for (int i = 0; i < securedMethods.size(); i++) {
        if (i == securedMethods.size() - 1) {
          isLast = true;
        }
        String commaStr = isLast ? "" : ",";

        SecuredMethod securedMethod = securedMethods.get(i);

        if (null != securedMethod.securedAction()) {
          out.printf(
              "\t\t\tMap.entry(\"%s\", SecuredUnion.securedAction(new SecuredAction(\"%s\", \"%s\","
                  + " List.of(%s))))%s%n",
              securedMethod.methodName(),
              securedMethod.securedAction().group(),
              securedMethod.securedAction().role(),
              String.join(
                  ", ",
                  securedMethod.securedAction().permissions().stream()
                      .map(s -> "\"" + s + "\"")
                      .toList()),
              commaStr);
        }

        if (null != securedMethod.roleBasedPermission()) {
          out.printf(
              "\t\t\tMap.entry(\"%s\", SecuredUnion.role(new Role(\"%s\", \"%s\")))%s%n",
              securedMethod.methodName(),
              securedMethod.roleBasedPermission().role(),
              securedMethod.roleBasedPermission().resource(),
              commaStr);
        }

        if (null != securedMethod.permissionBasedPermission()) {
          out.printf(
              "\t\t\tMap.entry(\"%s\", SecuredUnion.permission(new Permission(\"%s\","
                  + " \"%s\")))%s%n",
              securedMethod.methodName(),
              securedMethod.permissionBasedPermission().permission(),
              securedMethod.permissionBasedPermission().resource(),
              commaStr);
        }

        if (null != securedMethod.wildcardBasedPermission()) {
          out.printf(
              "\t\t\tMap.entry(\"%s\", SecuredUnion.wildcard(new Wildcard(\"%s\", \"%s\")))%s%n",
              securedMethod.methodName(),
              securedMethod.wildcardBasedPermission().permission(),
              securedMethod.wildcardBasedPermission().resource(),
              commaStr);
        }
      }
      out.println("\t\t);");
      out.println("\t}");
      out.println("}");
    }
  }

  private static List<SecuredMethod> getSecuredMethods(Element elementToBeAdvised) {
    List<SecuredMethod> securedMethods = new ArrayList<>();

    elementToBeAdvised.getEnclosedElements().stream()
        .filter(e -> ElementKind.METHOD == e.getKind())
        .forEach(
            e -> {
              addSecuredAction(e, securedMethods);
              addRole(e, securedMethods);
              addPermission(e, securedMethods);
              addWildcard(e, securedMethods);
            });

    return securedMethods;
  }

  private static void addSecuredAction(Element e, List<SecuredMethod> securedMethods) {
    var annotation = e.getAnnotation(SecuredProxy.SecuredAction.class);
    if (null == annotation) {
      return;
    }
    SecuredMethod sm =
        new SecuredMethod(
            e.getSimpleName().toString(),
            new SecuredMethod.SecuredAction(
                annotation.group(), annotation.role(), List.of(annotation.permissions())),
            null,
            null,
            null);
    securedMethods.add(sm);
  }

  private static void addRole(Element e, List<SecuredMethod> securedMethods) {
    var annotation = e.getAnnotation(SecuredProxy.RoleBasedPermission.class);
    if (null == annotation) {
      return;
    }
    SecuredMethod sm =
        new SecuredMethod(
            e.getSimpleName().toString(),
            null,
            new SecuredMethod.RoleBasedPermission(annotation.role(), annotation.resource()),
            null,
            null);
    securedMethods.add(sm);
  }

  private static void addPermission(Element e, List<SecuredMethod> securedMethods) {
    var annotation = e.getAnnotation(SecuredProxy.PermissionBasedPermission.class);
    if (null == annotation) {
      return;
    }
    SecuredMethod sm =
        new SecuredMethod(
            e.getSimpleName().toString(),
            null,
            null,
            new SecuredMethod.PermissionBasedPermission(
                annotation.permission(), annotation.resource()),
            null);
    securedMethods.add(sm);
  }

  private static void addWildcard(Element e, List<SecuredMethod> securedMethods) {
    var annotation = e.getAnnotation(SecuredProxy.WildcardBasedPermission.class);
    if (null == annotation) {
      return;
    }
    SecuredMethod sm =
        new SecuredMethod(
            e.getSimpleName().toString(),
            null,
            null,
            null,
            new SecuredMethod.WildcardBasedPermission(
                annotation.permission(), annotation.resource()));
    securedMethods.add(sm);
  }

  private record SecuredMethod(
      String methodName,
      SecuredAction securedAction,
      RoleBasedPermission roleBasedPermission,
      PermissionBasedPermission permissionBasedPermission,
      WildcardBasedPermission wildcardBasedPermission) {

    record SecuredAction(String group, String role, List<String> permissions) {}

    record RoleBasedPermission(String role, String resource) {}

    record PermissionBasedPermission(String permission, String resource) {}

    record WildcardBasedPermission(String permission, String resource) {}
  }
}
