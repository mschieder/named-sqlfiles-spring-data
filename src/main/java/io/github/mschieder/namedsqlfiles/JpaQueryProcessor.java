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
import java.util.List;
import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

/**
 * This annotation processor converts sql references to classpath resources in Spring Data JPA's
 * Query annotation parameters "name" and "countName" to standard named properties file
 * 'META-INF/jpa-named-queries.properties'.
 *
 * @author Michael Schieder
 */
@SupportedAnnotationTypes(JpaQueryProcessor.JPA_QUERY_ANNOTATION_TYPE)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class JpaQueryProcessor extends QueryProcessor {
  public static final String JPA_QUERY_ANNOTATION_TYPE =
      "org.springframework.data.jpa.repository.Query";
  private static final List<String> JPA_QUERY_ANNOTATION_PARAMETER_NAMES =
      List.of("name", "countName");
  private static final String DEFAULT_JPA_NAMED_QUERIES_PROPERTIES =
      "META-INF/jpa-named-queries.properties";

  /** Required no-argument constructor. */
  public JpaQueryProcessor() {
    super(
        JPA_QUERY_ANNOTATION_TYPE,
        DEFAULT_JPA_NAMED_QUERIES_PROPERTIES,
        JPA_QUERY_ANNOTATION_PARAMETER_NAMES);
  }
}
