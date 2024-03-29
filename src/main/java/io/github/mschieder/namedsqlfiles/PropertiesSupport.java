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
import java.io.OutputStream;
import java.util.Optional;
import java.util.Properties;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;

/**
 * Merges and stores properties files.
 *
 * @author Michael Schieder
 */
class PropertiesSupport {
  private final ResourceLocator resourceLocator;
  private final String propertiesFilename;
  private final ProcessingEnvironment processingEnv;

  PropertiesSupport(
      ResourceLocator resourceLocator,
      String propertiesFilename,
      ProcessingEnvironment processingEnv) {
    this.resourceLocator = resourceLocator;
    this.propertiesFilename = propertiesFilename;
    this.processingEnv = processingEnv;
  }

  void storePropertiesFile(Properties properties) {
    if (!properties.isEmpty()) {
      // merge existing properties
      var merged = mergeProperties(properties);

      try (OutputStream outputStream = createPropertiesFile()) {
        merged.store(outputStream, "generated by " + this.getClass().getSimpleName());
      } catch (IOException e) {
        processingEnv
            .getMessager()
            .printMessage(
                Diagnostic.Kind.ERROR,
                "unable to create file '" + propertiesFilename + "' not found.");
      }
    }
  }

  private Properties mergeProperties(Properties properties) {
    Properties merged = findExistingProperties().orElse(new Properties());
    merged.putAll(properties);
    return merged;
  }

  private Optional<Properties> findExistingProperties() {
    try {
      if (resourceLocator.exists(propertiesFilename)) {
        Properties properties = new Properties();
        properties.load(resourceLocator.resourceInputstream(propertiesFilename));
        return Optional.of(properties);
      }
    } catch (IOException e) {
      processingEnv
          .getMessager()
          .printMessage(
              Diagnostic.Kind.ERROR, "error loading properties '" + propertiesFilename + "'.");
    }
    return Optional.empty();
  }

  private OutputStream createPropertiesFile() throws IOException {
    var file =
        processingEnv
            .getFiler()
            .createResource(StandardLocation.CLASS_OUTPUT, "", propertiesFilename);
    return file.openOutputStream();
  }
}
