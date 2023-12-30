package io.github.mschieder.namedsqlfiles;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.JavaFileObjects.forResource;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.MapAssert;

public class QueryProcessorTestBase {

  protected String expectedGeneratedPropertiesFile;
  protected Map<String, String> expectedPropertiesFileEntries;

  protected Compilation compilation;

  protected void whenCompilePersonExample(String exampleName) {
    this.compilation =
        Compiler.javac()
            .withProcessors(createProcessor(exampleName))
            .compile(
                forResource(exampleName + "/Person.java"),
                forResource(exampleName + "/PersonRepository.java"));
  }

  protected Processor createProcessor(String exampleName) {
    return new TestableJpaQueryProcessor(exampleName);
  }

  protected void assertPersonCompilationSuccess() {
    assertThat(compilation).succeeded();

    Assertions.assertThat(compilation.generatedFiles().stream().map(JavaFileObject::getName))
        .containsExactlyInAnyOrder(
            "/CLASS_OUTPUT/io/github/mschieder/namedsqlfiles/Person.class",
            "/CLASS_OUTPUT/io/github/mschieder/namedsqlfiles/PersonRepository.class",
            "/CLASS_OUTPUT/" + expectedGeneratedPropertiesFile);

    assertThat(compilation)
        .generatedFile(StandardLocation.CLASS_OUTPUT, expectedGeneratedPropertiesFile)
        .contentsAsString(StandardCharsets.ISO_8859_1)
        .isNotEmpty();

    assertThatGeneratedNamedQueriesPropertiesFile()
        .containsAllEntriesOf(expectedPropertiesFileEntries);
  }

  protected MapAssert<Object, Object> assertThatGeneratedNamedQueriesPropertiesFile() {
    Properties properties = new Properties();
    try {
      properties.load(
          compilation
              .generatedFile(StandardLocation.CLASS_OUTPUT, expectedGeneratedPropertiesFile)
              .orElseThrow()
              .openInputStream());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return Assertions.assertThat(properties);
  }

  protected void assertPersonCompilationSuccessWithNoGeneratedPropertiesFile() {
    assertThat(compilation).succeeded();

    Assertions.assertThat(compilation.generatedFiles().stream().map(JavaFileObject::getName))
        .containsExactlyInAnyOrder(
            "/CLASS_OUTPUT/io/github/mschieder/namedsqlfiles/Person.class",
            "/CLASS_OUTPUT/io/github/mschieder/namedsqlfiles/PersonRepository.class");
  }
}
