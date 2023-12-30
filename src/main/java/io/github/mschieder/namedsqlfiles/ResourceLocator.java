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
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Locate and read resources with {@link ProcessingEnvironment}'s {@link
 * javax.annotation.processing.Filer}.
 *
 * @author Michael Schieder
 */
class ResourceLocator {

  private final ProcessingEnvironment processingEnv;

  public ResourceLocator(ProcessingEnvironment processingEnv) {
    this.processingEnv = processingEnv;
  }

  boolean exists(String resourceName) {
    return findResource(resourceName).isPresent();
  }

  String contentAsString(String resource) throws IOException {
    try (var is = resourceInputstream(resource)) {
      return resourceStreamToString(is);
    }
  }

  InputStream resourceInputstream(String resource) throws IOException {
    FileObject fileObject =
        findResource(resource).orElseThrow(() -> new FileNotFoundException(resource));
    return fileObject.openInputStream();
  }

  protected String resourceStreamToString(InputStream inputStream) throws IOException {
    StringBuilder builder = new StringBuilder();
    try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      char[] buffer = new char[8 * 1024];
      int read;
      while ((read = reader.read(buffer, 0, buffer.length)) != -1) {
        builder.append(buffer, 0, read);
      }
    }
    return builder.toString();
  }

  protected Optional<FileObject> findResource(String resourceName) {
    try {
      FileObject fileObject =
          processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);

      if (fileObject.getLastModified() != 0) {

        return Optional.of(fileObject);
      }
    } catch (IOException e) {
      // ignore
    }
    return Optional.empty();
  }
}
