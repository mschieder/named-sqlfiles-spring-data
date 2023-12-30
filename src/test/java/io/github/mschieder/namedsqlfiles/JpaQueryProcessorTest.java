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

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JpaQueryProcessorTest extends QueryProcessorTestBase {

  @BeforeEach
  void setup() {
    compilation = null;
    expectedGeneratedPropertiesFile = "META-INF/jpa-named-queries.properties";
    expectedPropertiesFileEntries =
        Map.of(
            "sql/person/all.sql",
            "select * from person",
            "sql/person/getByLastname.sql",
            "select p.* from person p where p.lastname = ?",
            "sql/person/count.sql",
            "select count(*)\r\n    from person");
  }

  @Test
  void test_01_missing_sql_resource_nok() {
    // given: repository with 3 referenced sqls, 1 sql resource is missing
    whenCompilePersonExample("jpa/01");

    // then: compilation fails
    assertThat(compilation).hadErrorCount(1);
    // then: the error points to the missing sql resource
    assertThat(compilation).hadErrorContaining("resource 'sql/person/all.sql' not found.");
  }

  @Test
  void test_02_sql_file_queries_only_ok() {
    // given: repository with sql file based query names only
    // when: compile
    whenCompilePersonExample("jpa/02");
    // then: compilation success with a generated properties file containing exact the 3 sql
    // queries
    assertPersonCompilationSuccess();
  }

  @Test
  void test_03_named_queries_only_ok() {
    // given: repository with 3 query names, possibly jpa
    // when: compile
    whenCompilePersonExample("jpa/03");
    // the: compilation success, no properties file was generated
    assertPersonCompilationSuccessWithNoGeneratedPropertiesFile();
  }

  @Test
  void test_04_existing_jpa_names_then_append_ok() {
    // given: repository with 3 query names and an existing properties file with 6 jpa queries
    whenCompilePersonExample("jpa/04");

    // then: compilation success, the 3 references queries are now in the properties file
    assertPersonCompilationSuccess();
    // then: the generated properties file contains also the 6 original queries
    assertThatGeneratedNamedQueriesPropertiesFile()
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
}
