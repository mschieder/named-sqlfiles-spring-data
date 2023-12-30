/*
 * Copyright 2023 Michael Schieder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mschieder.namedsqlfiles;

import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

/**
 * This annotation processor converts sql references to classpath resources in Spring Data's Query
 * annotation parameters "name" and "countName" to standard named properties file
 * 'META-INF/jpa-named-queries.properties'.
 *
 * @author Michael Schieder
 */
@SupportedAnnotationTypes(QueryProcessor.QUERY_ANNOTATION_TYPE)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class QueryProcessor extends AbstractProcessor {

  public static final String QUERY_ANNOTATION_TYPE =
      "org.springframework.data.jpa.repository.Query";
  private static final String JPA_NAMED_QUERIES_PROPERTIES =
      "META-INF/jpa-named-queries.properties";

  protected ResourceLocator resourceLocator() {
    return new ResourceLocator(processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!roundEnv.processingOver()) {
      Properties properties = createOrLoadProperties();

      annotations.stream()
          .flatMap(next -> roundEnv.getElementsAnnotatedWith(next).stream())
          .forEach(annotatedElement -> processNamedQueryProperties(annotatedElement, properties));

      writePropertiesFile(properties);
    }
    return true;
  }

  private Properties createOrLoadProperties() {
    Properties properties = new Properties();
    try {
      if (resourceLocator().exists(JPA_NAMED_QUERIES_PROPERTIES)) {
        properties.load(resourceLocator().resourceInputstream(JPA_NAMED_QUERIES_PROPERTIES));
      }
    } catch (IOException e) {
      processingEnv
          .getMessager()
          .printMessage(
              Diagnostic.Kind.ERROR,
              "error loading properties '" + JPA_NAMED_QUERIES_PROPERTIES + "'.");
    }
    return properties;
  }

  private void processNamedQueryProperties(Element element, Properties properties) {
    findQueryAnnotation(element)
        .ifPresent(
            query -> {
              addProperty(findQueryElement("name", query), properties, element);
              addProperty(findQueryElement("countName", query), properties, element);
            });
  }

  private Optional<? extends AnnotationMirror> findQueryAnnotation(Element element) {
    return element.getAnnotationMirrors().stream()
        .filter(m -> QUERY_ANNOTATION_TYPE.equals(m.getAnnotationType().toString()))
        .findFirst();
  }

  private String findQueryElement(String elementName, AnnotationMirror queryAnnotation) {
    return queryAnnotation.getElementValues().entrySet().stream()
        .filter(entry -> elementName.equals(entry.getKey().getSimpleName().toString()))
        .map(entry -> entry.getValue().getValue().toString())
        .findFirst()
        .orElse("");
  }

  private void addProperty(String name, Properties properties, Element element) {
    getContentFromResource(name, element).ifPresent(sql -> properties.put(name, sql));
  }

  private Optional<String> getContentFromResource(String resourceName, Element element) {
    if (resourceName != null && !resourceName.isBlank()) {
      var locator = resourceLocator();

      if (locator.exists(resourceName)) {
        try {
          return Optional.of(locator.contentAsString(resourceName));
        } catch (IOException e) {
          processingEnv
              .getMessager()
              .printMessage(
                  Diagnostic.Kind.ERROR,
                  "error while reading resource '" + resourceName + "': " + e.getMessage(),
                  element);
        }
      } else {
        if (resourceName.toLowerCase().endsWith(".sql")) {
          // only if the suffix is sql
          processingEnv
              .getMessager()
              .printMessage(
                  Diagnostic.Kind.ERROR, "resource '" + resourceName + "' not found.", element);
        }
      }
    }
    return Optional.empty();
  }

  private void writePropertiesFile(Properties properties) {
    if (!properties.isEmpty()) {
      try (OutputStream outputStream = createPropertiesFile()) {
        properties.store(outputStream, "generated by " + this.getClass().getSimpleName());
      } catch (IOException e) {
        processingEnv
            .getMessager()
            .printMessage(
                Diagnostic.Kind.ERROR,
                "unable to create file '" + JPA_NAMED_QUERIES_PROPERTIES + "' not found.");
      }
    }
  }

  private OutputStream createPropertiesFile() throws IOException {
    var file =
        processingEnv
            .getFiler()
            .createResource(StandardLocation.CLASS_OUTPUT, "", JPA_NAMED_QUERIES_PROPERTIES);
    return file.openOutputStream();
  }
}
