package io.github.mschieder.namedsqlfiles;

import static com.google.testing.compile.CompilationSubject.assertThat;

import java.util.Map;
import javax.annotation.processing.Processor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcQueryProcessorTest extends QueryProcessorTestBase {

  @BeforeEach
  void prepare() {
    expectedGeneratedPropertiesFile = "META-INF/jdbc-named-queries.properties";
    expectedPropertiesFileEntries =
        Map.of(
            "sql/person/all.sql",
            "select * from person",
            "sql/person/getByLastname.sql",
            "select p.id, p.firstname, p.lastname from person p where p.lastname = :lastname");
  }

  @Override
  protected Processor createProcessor(String exampleName) {
    return new TestableJdbcQueryProcessor(exampleName);
  }

  @Test
  void test_01_missing_sql_resource_nok() {
    // given: repository with 2 referenced sqls, 1 sql resource is missing
    whenCompilePersonExample("jdbc/01");

    // then: compilation fails
    assertThat(compilation).hadErrorCount(1);
    // then: the error points to the missing sql resource
    assertThat(compilation).hadErrorContaining("resource 'sql/person/all.sql' not found.");
  }

  @Test
  void test_02_sql_file_queries_only_ok() {
    // given: repository with sql file based query names only
    // when: compile
    whenCompilePersonExample("jdbc/02");
    // then: compilation success with a generated properties file containing exact the 3 sql
    // queries
    assertPersonCompilationSuccess();
  }

  @Test
  void test_03_named_queries_only_ok() {
    // given: repository with 3 query names, possibly jpa
    // when: compile
    whenCompilePersonExample("jdbc/03");
    // the: compilation success, no properties file was generated
    assertPersonCompilationSuccessWithNoGeneratedPropertiesFile();
  }

  @Test
  void test_04_existing_named_queries_then_append_ok() {
    // given: repository with 3 query names and an existing properties file with 6 jpa queries
    whenCompilePersonExample("jdbc/04");

    // then: compilation success, the 2 references queries are now in the properties file
    assertPersonCompilationSuccess();
    // then: the generated properties file contains also the 6 original queries
    assertThatGeneratedNamedQueriesPropertiesFile()
        .containsAllEntriesOf(
            Map.of("TESTQUERY", "first", "TESTQUERY2", "second", "TESTQUERY3", "third"));
  }
}
