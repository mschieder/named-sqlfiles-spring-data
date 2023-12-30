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

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.assertj.core.api.MapAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryProcessorTest {

  private Compilation compilation;

  @BeforeEach
  void setup() {
    compilation = null;
  }

  @Test
  void test_example_01_missing_sql_resource_nok() {
    // given: repository with 3 referenced sqls, 1 sql resource is missubg
    whenCompilePersonExample("example_01_nok");

    // then: compilation fails
    assertThat(compilation).hadErrorCount(1);
    // then: the error points to the missing sql resource
    assertThat(compilation).hadErrorContaining("resource 'sql/person/all.sql' not found.");
  }

  @Test
  void test_example_02_sql_file_queries_only_ok() {
    // given: repository with sql file based query names only
    // when: compile
    whenCompilePersonExample("example_02_ok");
    // then: compilation success with a generated properties file containing exact the 3 sql
    // queries
    assertPersonCompilationSuccess();
  }

  @Test
  void test_example_03_jpa_named_queries_only_ok() {
    // given: repository with 3 query names, possibly jpa
    // when: compile
    whenCompilePersonExample("example_03_ok");
    // the: compilation success, no properties file was generated
    assertPersonCompilationSuccessWithNoGeneratedPropertiesFile();
  }

  @Test
  void test_example_04_existing_jpa_names_then_append_ok() {
    // given: repository with 3 query names and an existing properties file with 6 jpa queries
    whenCompilePersonExample("example_04_ok");

    // then: compilation success, the 3 references queries are now in the properties file
    assertPersonCompilationSuccess();
    // then: the generated properties file contains also the 6 original queries
    assertThatJpaNamedQueriesPropertieFile()
        .containsAllEntriesOf(
            Map.of(
                "TESTQUERY",
                "first",
                "TESTQUERY.COUNT",
                "second",
                "TESTQUERY2",
                "third",
                "TESTQUERY2.COUNT",
                "fourth",
                "TESTQUERY3",
                "fifth",
                "TESTQUERY3.COUNT",
                "sixth"));
  }

  private void assertPersonCompilationSuccessWithNoGeneratedPropertiesFile() {
    assertThat(compilation).succeeded();

    assertThat(compilation.generatedFiles().stream().map(JavaFileObject::getName))
        .containsExactlyInAnyOrder(
            "/CLASS_OUTPUT/io/github/mschieder/spring/example/Person.class",
            "/CLASS_OUTPUT/io/github/mschieder/spring/example/PersonRepository.class");
  }

  private void assertPersonCompilationSuccess() {
    assertThat(compilation).succeeded();

    assertThat(compilation.generatedFiles().stream().map(JavaFileObject::getName))
        .containsExactlyInAnyOrder(
            "/CLASS_OUTPUT/io/github/mschieder/spring/example/Person.class",
            "/CLASS_OUTPUT/io/github/mschieder/spring/example/PersonRepository.class",
            "/CLASS_OUTPUT/META-INF/jpa-named-queries.properties");

    assertThat(compilation)
        .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/jpa-named-queries.properties")
        .contentsAsString(StandardCharsets.ISO_8859_1)
        .isNotEmpty();

    assertThatJpaNamedQueriesPropertieFile()
        .containsAllEntriesOf(
            Map.of(
                "sql/person/all.sql",
                "select * from person",
                "sql/person/getByLastname.sql",
                "select p.* from person p where p.lastname = ?",
                "sql/person/count.sql",
                "select count(*)\r\n    from person"));
  }

  private MapAssert<Object, Object> assertThatJpaNamedQueriesPropertieFile() {
    Properties properties = new Properties();
    try {
      properties.load(
          compilation
              .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/jpa-named-queries.properties")
              .orElseThrow()
              .openInputStream());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return assertThat(properties);
  }

  private void whenCompilePersonExample(String exampleName) {
    this.compilation =
        Compiler.javac()
            .withProcessors(new TestableQueryProcessor(exampleName))
            .compile(
                forResource(exampleName + "/Person.java"),
                forResource(exampleName + "/PersonRepository.java"));
  }
}
