/* Licensed under Apache-2.0 2024. */
package github.benslabbert.vertxdaggercodegen.serviceproxy;

import github.benslabbert.vertxdaggercodegen.annotation.serviceproxy.GenerateProxies;
import github.benslabbert.vertxdaggercodegen.commons.GenerationException;
import github.benslabbert.vertxdaggercodegen.commons.TypeWithImports;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

public class ServiceProxyGenerator extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(GenerateProxies.class.getCanonicalName());
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

  private void process(Element serviceClassElement) throws IOException {
    System.err.println("Processing element: " + serviceClassElement);

    processClient(serviceClassElement);
    processServer(serviceClassElement);
  }

  private void processClient(Element serviceClassElement) throws IOException {
    System.err.println("Client annotation found");

    // generate a concrete class with implements to provided interface
    if (ElementKind.INTERFACE != serviceClassElement.getKind()) {
      throw new GenerationException("Client annotation can only be used on interfaces");
    }

    List<ExecutableElement> methodsToOverride =
        serviceClassElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .filter(e -> !e.getModifiers().contains(Modifier.DEFAULT))
            .map(e -> (ExecutableElement) e)
            .toList();

    Set<String> imports =
        methodsToOverride.stream()
            .map(
                e -> {
                  var rt = TypeWithImports.of(e.getReturnType());
                  if (!rt.canonicalImports().contains("io.vertx.core.Future")) {
                    throw new GenerationException(
                        "Method return type must be io.vertx.core.Future");
                  }

                  List<? extends VariableElement> parameters = e.getParameters();

                  if (parameters.size() != 1) {
                    throw new GenerationException("Method must have exactly one parameter");
                  }

                  Stream<String> paramImportStream =
                      TypeWithImports.of(parameters.getFirst().asType())
                          .canonicalImports()
                          .stream();
                  Stream<String> stream = rt.canonicalImports().stream();
                  return Stream.concat(stream, paramImportStream).collect(Collectors.toSet());
                })
            .flatMap(Set::stream)
            .filter(f -> !f.startsWith("java.lang."))
            .collect(Collectors.toSet());

    String canonicalName = serviceClassElement.asType().toString();
    String classPackage = canonicalName.substring(0, canonicalName.lastIndexOf('.'));
    Name interfaceSimpleName = serviceClassElement.getSimpleName();
    String generatedClassName = interfaceSimpleName + "VertxEBClientProxy";

    // create the class
    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.printf("package %s;%n", classPackage);
      out.println();

      imports.forEach(anImport -> out.printf("import %s;%n", anImport));
      out.println("import io.vertx.serviceproxy.ServiceException;");
      out.println("import io.vertx.serviceproxy.ServiceExceptionMessageCodec;");
      out.println("import io.vertx.core.json.JsonObject;");
      out.println("import io.vertx.core.eventbus.DeliveryOptions;");
      out.println("import io.vertx.core.Vertx;");
      out.println("import io.vertx.core.Future;");
      out.println("import javax.annotation.processing.Generated;");
      out.println();

      out.printf(
          "@Generated(value = \"%s\", date = \"%s\")%n",
          getClass().getCanonicalName(),
          LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
      out.println(
          "public class " + generatedClassName + " implements " + interfaceSimpleName + " {");
      out.println();
      out.println("\tprivate Vertx _vertx;");
      out.println("\tprivate String _address;");
      out.println("\tprivate DeliveryOptions _options;");
      out.println();

      // generate constructors
      out.printf("\tpublic %s(Vertx vertx, String address) {%n", generatedClassName);
      out.println("\t\tthis(vertx, address, null);");
      out.println("\t}");
      out.println();

      out.printf(
          "\tpublic %s(Vertx vertx, String address, DeliveryOptions options) {%n",
          generatedClassName);
      out.println("\t\tthis._vertx = vertx;");
      out.println("\t\tthis._address = address;");
      out.println("\t\tthis._options = options;");

      out.println("\t\ttry {");
      out.println("\t\t\tthis._vertx");
      out.println("\t\t\t\t.eventBus()");
      out.println(
          "\t\t\t\t.registerDefaultCodec(ServiceException.class, new"
              + " ServiceExceptionMessageCodec());");
      out.println("\t\t} catch (IllegalStateException ex) {");
      out.println("\t\t\t// ignore");
      out.println("\t\t}");
      out.println("\t}");
      out.println();

      // generate method
      for (ExecutableElement overrideMethod : methodsToOverride) {
        var rt = TypeWithImports.of(overrideMethod.getReturnType());
        // only one, validated above
        String paramName =
            TypeWithImports.of(overrideMethod.getParameters().getFirst().asType()).printableName();

        out.println("\t@Override");
        out.printf(
            "\tpublic %s %s(%s req) {%n",
            rt.printableName(), overrideMethod.getSimpleName(), paramName);
        out.println("\t\tJsonObject _json = new JsonObject();");
        out.println("\t\t_json.put(\"request\", req.toJson());");
        out.println();

        out.println(
            "\t\tDeliveryOptions _deliveryOptions = (_options != null) ? new"
                + " DeliveryOptions(_options) : new DeliveryOptions();");
        out.printf(
            "\t\t_deliveryOptions.addHeader(\"action\", \"%s\");%n",
            overrideMethod.getSimpleName().toString());
        out.printf(
            "\t\t_deliveryOptions.getHeaders().set(\"action\", \"%s\");%n",
            overrideMethod.getSimpleName().toString());

        out.println("\t\treturn _vertx");
        out.println("\t\t\t\t.eventBus()");
        out.println("\t\t\t\t.<JsonObject>request(_address, _json, _deliveryOptions)");
        out.println("\t\t\t\t.map(msg -> {");
        String genericType = getGenericType(rt.printableName());
        out.printf(
            "\t\t\t\t\treturn msg.body() != null ? %s.fromJson(msg.body()) : null;%n", genericType);
        out.println("\t\t\t\t});");
        out.println("\t}");
        out.println();
      }

      out.println("}");
    }
  }

  private void processServer(Element serviceClassElement) throws IOException {
    System.err.println("Server annotation found");

    // generate a concrete class with implements to provided interface
    if (ElementKind.INTERFACE != serviceClassElement.getKind()) {
      throw new GenerationException("Server annotation can only be used on interfaces");
    }

    List<ExecutableElement> methodsToOverride =
        serviceClassElement.getEnclosedElements().stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .filter(e -> !e.getModifiers().contains(Modifier.DEFAULT))
            .map(e -> (ExecutableElement) e)
            .toList();

    Set<String> methodParamImports =
        methodsToOverride.stream()
            .map(
                e -> {
                  List<? extends VariableElement> parameters = e.getParameters();

                  if (parameters.size() != 1) {
                    throw new GenerationException("Method must have exactly one parameter");
                  }

                  return TypeWithImports.of(parameters.getFirst().asType()).canonicalImports();
                })
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    String canonicalName = serviceClassElement.asType().toString();
    String classPackage = canonicalName.substring(0, canonicalName.lastIndexOf('.'));
    Name interfaceSimpleName = serviceClassElement.getSimpleName();
    String generatedClassName = interfaceSimpleName + "VertxEBProxyHandler";

    // create the class
    JavaFileObject builderFile =
        processingEnv.getFiler().createSourceFile(classPackage + "." + generatedClassName);

    // get imports for the method params
    try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
      out.printf("package %s;%n", classPackage);
      out.println();
      out.println("import io.vertx.core.Vertx;");
      out.println("import io.vertx.core.eventbus.Message;");
      out.println("import io.vertx.core.json.JsonObject;");
      out.println("import io.vertx.serviceproxy.HelperUtils;");
      out.println("import io.vertx.serviceproxy.ProxyHandler;");
      out.println("import io.vertx.serviceproxy.ServiceException;");
      out.println("import io.vertx.serviceproxy.ServiceExceptionMessageCodec;");
      methodParamImports.forEach(anImport -> out.printf("import %s;%n", anImport));
      out.println("import javax.annotation.processing.Generated;");
      out.println();

      out.printf(
          "@Generated(value = \"%s\", date = \"%s\")%n",
          getClass().getCanonicalName(),
          LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
      out.println("public class " + generatedClassName + " extends ProxyHandler {");
      out.println();

      out.println("\tpublic static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes");
      out.println("\tprivate final Vertx vertx;");
      out.printf("\tprivate final %s service;%n", interfaceSimpleName);
      out.println("\tprivate final long timerID;");
      out.println("\tprivate long lastAccessed;");
      out.println("\tprivate final long timeoutSeconds;");
      out.println("\tprivate final boolean includeDebugInfo;");
      out.println();

      // constructors
      out.printf(
          "\tpublic %s(Vertx vertx, %s service) {%n", generatedClassName, interfaceSimpleName);
      out.println("\t\tthis(vertx, service, DEFAULT_CONNECTION_TIMEOUT);");
      out.println("\t}");
      out.println();

      out.printf(
          "\tpublic %s(Vertx vertx, %s service, long timeoutInSecond) {%n",
          generatedClassName, interfaceSimpleName);
      out.println("\t\tthis(vertx, service, true, timeoutInSecond);");
      out.println("\t}");
      out.println();

      out.printf(
          "\tpublic %s(Vertx vertx, %s service, boolean topLevel, long timeoutInSecond) {%n",
          generatedClassName, interfaceSimpleName);
      out.println("\t\tthis(vertx, service, topLevel, timeoutInSecond, false);");
      out.println("\t}");
      out.println();

      out.printf(
          "\tpublic %s(Vertx vertx, %s service, boolean topLevel, long timeoutSeconds, boolean"
              + " includeDebugInfo) {%n",
          generatedClassName, interfaceSimpleName);
      out.println("\t\tthis.vertx = vertx;");
      out.println("\t\tthis.service = service;");
      out.println("\t\tthis.includeDebugInfo = includeDebugInfo;");
      out.println("\t\tthis.timeoutSeconds = timeoutSeconds;");
      out.println("\t\ttry {");
      out.println("\t\t\tthis.vertx");
      out.println("\t\t\t\t.eventBus()");
      out.println(
          "\t\t\t\t.registerDefaultCodec(ServiceException.class, new"
              + " ServiceExceptionMessageCodec());");
      out.println("\t\t} catch (IllegalStateException ex) {");
      out.println("\t\t\t// ignore");
      out.println("\t\t}");
      out.println("\t\tif (timeoutSeconds != -1 && !topLevel) {");
      out.println("\t\t\tlong period = timeoutSeconds * 1000 / 2;");
      out.println("\t\t\tif (period > 10000) {");
      out.println("\t\t\t\tperiod = 10000;");
      out.println("\t\t\t}");
      out.println("\t\t\tthis.timerID = vertx.setPeriodic(period, this::checkTimedOut);");
      out.println("\t\t} else {");
      out.println("\t\t\tthis.timerID = -1;");
      out.println("\t\t}");
      out.println("\t\taccessed();");
      out.println("\t}");
      out.println();

      out.println("\tprivate void checkTimedOut(long id) {");
      out.println("\t\tlong now = System.nanoTime();");
      out.println("\t\tif (now - lastAccessed > timeoutSeconds * 1000000000) {");
      out.println("\t\t\tclose();");
      out.println("\t\t}");
      out.println("\t}");
      out.println();
      out.println("\t@Override");
      out.println("\tpublic void close() {");
      out.println("\t\tif (timerID != -1) {");
      out.println("\t\t\tvertx.cancelTimer(timerID);");
      out.println("\t\t}");
      out.println("\t\tsuper.close();");
      out.println("\t}");

      out.println();
      out.println("\tprivate void accessed() {");
      out.println("\t\tthis.lastAccessed = System.nanoTime();");
      out.println("\t}");

      out.println();

      out.println("\t@Override");
      out.println("\tpublic void handle(Message<JsonObject> msg) {");
      out.println("\t\ttry {");
      out.println("\t\t\tJsonObject json = msg.body();");
      out.println("\t\t\tString action = msg.headers().get(\"action\");");
      out.println("\t\t\tif (action == null) {");
      out.println("\t\t\t\tthrow new IllegalStateException(\"action not specified\");");
      out.println("\t\t\t}");
      out.println("\t\t\taccessed();");
      out.println("\t\t\tswitch (action) {");

      for (ExecutableElement ee : methodsToOverride) {
        String paramName =
            TypeWithImports.of(ee.getParameters().getFirst().asType()).printableName();
        String methodName = ee.getSimpleName().toString();
        out.printf("\t\t\t\tcase \"%s\" -> {%n", methodName);
        out.println("\t\t\t\t\tservice");
        out.printf(
            "\t\t\t\t\t\t.%s(json.getJsonObject(\"request\") != null ?"
                + " %s.fromJson(json.getJsonObject(\"request\")) : null)%n",
            methodName, paramName);
        out.println("\t\t\t\t\t\t.onComplete(");
        out.println("\t\t\t\t\t\t\tres -> {");
        out.println("\t\t\t\t\t\t\t\tif (res.failed()) {");
        out.println(
            "\t\t\t\t\t\t\t\t\tHelperUtils.manageFailure(msg, res.cause(), includeDebugInfo);");
        out.println("\t\t\t\t\t\t\t\t} else {");
        out.println(
            "\t\t\t\t\t\t\t\t\tmsg.reply(res.result() != null ? res.result().toJson() : null);");
        out.println("\t\t\t\t\t\t\t\t}");
        out.println("\t\t\t\t\t\t});");
        out.println("\t\t\t\t\t}");
      }

      out.println(
          "\t\t\t\tdefault -> throw new IllegalStateException(\"Invalid action: \" + action);");
      out.println("\t\t\t}");
      out.println("\t\t} catch (Throwable t) {");
      out.println("\t\t\tif (includeDebugInfo) {");
      out.println(
          "\t\t\t\tmsg.reply(new ServiceException(500, t.getMessage(),"
              + " HelperUtils.generateDebugInfo(t)));");
      out.println("\t\t\t}");
      out.println("\t\t\telse {");
      out.println("\t\t\t\tmsg.reply(new ServiceException(500, t.getMessage()));");
      out.println("\t\t\t}");
      out.println("\t\t\tthrow t;");
      out.println("\t\t}");

      out.println("\t}");

      out.println("}");
    }
  }

  private static String getGenericType(String in) {
    int start = in.indexOf('<') + 1;
    int end = in.indexOf('>');
    return in.substring(start, end);
  }
}
