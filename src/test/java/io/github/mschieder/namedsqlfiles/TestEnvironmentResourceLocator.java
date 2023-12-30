package io.github.mschieder.namedsqlfiles;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class TestEnvironmentResourceLocator extends ResourceLocator {

  private final String prefixPath;

  public TestEnvironmentResourceLocator(String prefixPath) {
    super(null);
    this.prefixPath = prefixPath;
  }

  @Override
  boolean exists(String resourceName) {
    return findClasspathResource(resourceName).isPresent();
  }

  @Override
  String contentAsString(String resource) throws IOException {
    try (var is =
        findClasspathResource(resource).orElseThrow(() -> new FileNotFoundException(resource))) {
      return resourceStreamToString(is);
    }
  }

  @Override
  InputStream resourceInputstream(String resource) throws IOException {
    return findClasspathResource(resource).orElseThrow(() -> new FileNotFoundException(resource));
  }

  private Optional<InputStream> findClasspathResource(String resourceName) {
    var inputStream = this.getClass().getResourceAsStream("/" + prefixPath + "/" + resourceName);

    return Optional.ofNullable(inputStream);
  }
}
