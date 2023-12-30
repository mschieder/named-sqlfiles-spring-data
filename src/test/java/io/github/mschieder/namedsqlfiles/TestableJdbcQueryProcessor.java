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

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

@SupportedAnnotationTypes(JdbcQueryProcessor.JDBC_QUERY_ANNOTATION_TYPE)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class TestableJdbcQueryProcessor extends JdbcQueryProcessor {

  private final TestEnvironmentResourceLocator resourceLocator;

  public TestableJdbcQueryProcessor(String prefixPath) {
    this.resourceLocator = new TestEnvironmentResourceLocator(prefixPath);
  }

  @Override
  ResourceLocator resourceLocator() {
    return resourceLocator;
  }
}
