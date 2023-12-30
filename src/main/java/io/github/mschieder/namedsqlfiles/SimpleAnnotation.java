package io.github.mschieder.namedsqlfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

/** Wrapper for an annotation mirror. */
class SimpleAnnotation {
  private final AnnotationMirror mirror;

  private SimpleAnnotation(AnnotationMirror mirror) {
    this.mirror = mirror;
  }

  public static Optional<SimpleAnnotation> findAnnotation(String annotationType, Element element) {
    return findAnnotationMirror(annotationType, element).map(SimpleAnnotation::new);
  }

  private static Optional<? extends AnnotationMirror> findAnnotationMirror(
      String annotationType, Element element) {
    return element.getAnnotationMirrors().stream()
        .filter(m -> annotationType.equals(m.getAnnotationType().toString()))
        .findFirst();
  }

  Map<String, String> getParameterMap(List<String> parameterNames) {
    return findAnnotationParameters(mirror, parameterNames);
  }

  private Map<String, String> findAnnotationParameters(
      AnnotationMirror query, List<String> parameterNames) {
    return parameterNames.stream()
        .collect(Collectors.toUnmodifiableMap(k -> k, v -> getAnnotationParameterValue(v, query)));
  }

  private String getAnnotationParameterValue(String elementName, AnnotationMirror queryAnnotation) {
    return queryAnnotation.getElementValues().entrySet().stream()
        .filter(entry -> elementName.equals(entry.getKey().getSimpleName().toString()))
        .map(entry -> entry.getValue().getValue().toString())
        .findFirst()
        .orElse("");
  }
}
