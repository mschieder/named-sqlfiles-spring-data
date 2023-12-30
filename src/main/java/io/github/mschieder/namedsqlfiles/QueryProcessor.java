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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Common annotation processing functionality for @{@link JdbcQueryProcessor} and {@link
 * JpaQueryProcessor}.
 *
 * @author Michael Schieder
 */
public abstract class QueryProcessor extends AbstractProcessor {

  private final String annotationType;
  private final String propertiesFilename;
  private final List<String> queryParameterNames;
  private final Properties properties;

  QueryProcessor(
      String annotationType, String propertiesFilename, List<String> queryParameterNames) {
    this.annotationType = annotationType;
    this.propertiesFilename = propertiesFilename;
    this.queryParameterNames = Collections.unmodifiableList(queryParameterNames);
    this.properties = new Properties();
  }

  ResourceLocator resourceLocator() {
    return new ResourceLocator(processingEnv);
  }

  private PropertiesSupport propertiesSupport() {
    return new PropertiesSupport(resourceLocator(), propertiesFilename, processingEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!roundEnv.processingOver()) {
      annotations.stream()
          .flatMap(next -> roundEnv.getElementsAnnotatedWith(next).stream())
          .forEach(this::processNamedQueryParameters);

      propertiesSupport().storePropertiesFile(properties);
    }
    return true;
  }

  private void processNamedQueryParameters(Element element) {
    SimpleAnnotation.findAnnotation(annotationType, element).stream()
        .flatMap(
            queryAnnotation ->
                queryAnnotation.getParameterMap(queryParameterNames).values().stream())
        .forEach(value -> processQueryParameter(value, properties, element));
  }

  private void processQueryParameter(String name, Properties properties, Element element) {
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
}
