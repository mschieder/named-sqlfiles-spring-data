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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

@SupportedAnnotationTypes(JpaQueryProcessor.JPA_QUERY_ANNOTATION_TYPE)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class TestableJpaQueryProcessor extends JpaQueryProcessor {

  private final String prefixPath;

  public TestableJpaQueryProcessor(String prefixPath) {
    this.prefixPath = prefixPath;
  }

  @Override
  ResourceLocator resourceLocator() {
    return new ResourceLocator(null) {

      @Override
      boolean exists(String resourceName) {
        return findClasspathResource(resourceName).isPresent();
      }

      @Override
      String contentAsString(String resource) throws IOException {
        try (var is =
            findClasspathResource(resource)
                .orElseThrow(() -> new FileNotFoundException(resource))) {
          return resourceStreamToString(is);
        }
      }

      @Override
      InputStream resourceInputstream(String resource) throws IOException {
        return findClasspathResource(resource)
            .orElseThrow(() -> new FileNotFoundException(resource));
      }

      private Optional<InputStream> findClasspathResource(String resourceName) {
        var inputStream =
            this.getClass().getResourceAsStream("/" + prefixPath + "/" + resourceName);

        return Optional.ofNullable(inputStream);
      }
    };
  }
}
